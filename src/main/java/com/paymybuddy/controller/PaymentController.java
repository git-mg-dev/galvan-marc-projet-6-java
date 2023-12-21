package com.paymybuddy.controller;

import com.paymybuddy.exceptions.NullUserException;
import com.paymybuddy.exceptions.PaymentFailedException;
import com.paymybuddy.exceptions.UserNotFountException;
import com.paymybuddy.model.*;
import com.paymybuddy.service.OperationService;
import com.paymybuddy.service.SecurityService;
import com.paymybuddy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PaymentController {
    @Autowired
    private OperationService operationService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserService userService;

    @GetMapping("/transfer")
    public String displayPayments(Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model, PaymentInfo paymentInfo,
                                  @RequestParam(required = false) Integer id, @RequestParam(required = false) Integer page) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);
        if(userAccount != null) {
            if(id == null) {
                id = 0;
            }
            if(page == null) {
                page = 0;
            } else if (page > 0) {
                --page;
            }
            addDataToModel(model, userAccount, id, page, "");
            return "/transfer";
        } else {
            return "redirect:index?error";
        }
    }

    @PostMapping("/transfer")
    public String sendPayment(@Valid PaymentInfo paymentInfo, BindingResult bindingResult, Principal user, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        UserAccount userAccount = securityService.getUserInfo(user, oidcUser);

        if (!bindingResult.hasErrors()) {
            try {
                userAccount = operationService.sendPayment(userAccount, paymentInfo);
                return "redirect:transfer?success";
            } catch (NullUserException | UserNotFountException | PaymentFailedException e) {
                userAccount = securityService.getUserInfo(user, oidcUser); //refresh user info
                addDataToModel(model, userAccount, paymentInfo.getRecipientId(), 0, e.getMessage());
                return "/transfer";
            }
        } else {
            addDataToModel(model, userAccount, 0, 0, "");
            return "/transfer";
        }
    }

    /**
     * Add attributes to the model so they can be displayed
     * @param model to add attributes to
     * @param userAccount user data
     * @param selectedContactId id of contact to send payment to
     * @param pageNumber to handle pagination of operation list
     * @param errorMessage error message to display
     */
    private void addDataToModel(Model model, UserAccount userAccount, Integer selectedContactId, int pageNumber, String errorMessage) {
        int listSize = 5;
        List<OperationDisplay> operationList = userService.getPaymentToDisplay(userAccount);
        List<OperationDisplay> operationDisplays = userService.getPaginatedListOfOperations(operationList, pageNumber*5, listSize);
        List<ContactDisplay> contactDisplays = userService.getContactToDisplay(userAccount);

        model.addAttribute("balance", userAccount.getAccountBalance());
        model.addAttribute("operations", operationDisplays);
        model.addAttribute("contacts", contactDisplays);
        model.addAttribute("selectedContactId", selectedContactId);

        // Handling pagination
        // Calculates number of pages
        int nbPages = operationList.size() / listSize;
        if(operationList.size() % listSize > 0) {
            nbPages++;
        }

        if(nbPages == 0 && !operationList.isEmpty()) {
            nbPages = 1;
        }

        // Handling previous and next links
        int currentPage = pageNumber + 1;

        int pagePrev = 0;
        if(currentPage > 1) {
            pagePrev = currentPage - 1;
        }

        int pageNext = currentPage + 1;
        if(currentPage >= nbPages) {
            pageNext = 0;
        }

        // Creating the list of numbers to display
        List<Integer> pageList = new ArrayList<>();
        if(!operationDisplays.isEmpty()) {
            int listStart = 0;
            int listEnd = 5;
            if (currentPage > 5 && nbPages > 5) { //nb max of pages to list in pagination
                listStart = currentPage - 3;
                listEnd = currentPage + 2;
            }
            while (listEnd > nbPages) {
                listStart--;
                listEnd--;
            }
            if(listStart < 0) {
                listStart = 0;
            }

            for (int i = listStart; i < listEnd; i++) {
                pageList.add(i + 1);
            }
        }

        model.addAttribute("pagePrev", pagePrev);
        model.addAttribute("pageNext", pageNext);
        model.addAttribute("pages", pageList);

        if(!errorMessage.isEmpty()) {
            model.addAttribute("transferError", "Payment failed: " + errorMessage);
        }
    }

}
