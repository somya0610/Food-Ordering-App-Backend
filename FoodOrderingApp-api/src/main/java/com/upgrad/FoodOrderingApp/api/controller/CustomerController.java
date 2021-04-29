package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.LoginResponse;
import com.upgrad.FoodOrderingApp.api.model.LogoutResponse;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Controller
@CrossOrigin
public class CustomerController {

    @Autowired
    CustomerService customerService;

    /**
     * RestController method called when the request pattern is of type '/customer/signup'
     * and the incoming request is of 'POST' type
     * Persists SignupCustomerRequest details in the database
     *
     * @param signupCustomerRequest - signup customer details
     * @return - ResponseEntity (SignupCustomerResponse along with HTTP status code)
     * @throws SignUpRestrictedException - if the required field information is missing, or does not pass validations,
     *                                  or customer with contact number already exists in the database
     */
    @RequestMapping(method = RequestMethod.POST, path = "/customer/signup",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(
            @RequestBody(required = false) final SignupCustomerRequest signupCustomerRequest)
            throws SignUpRestrictedException {

        // Throw exception if any of the mandatory field value does not exist
        if (signupCustomerRequest.getFirstName().isEmpty()
                || signupCustomerRequest.getEmailAddress().isEmpty()
                || signupCustomerRequest.getContactNumber().isEmpty()
                || signupCustomerRequest.getPassword().isEmpty()) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }

        // Set CustomerEntity fields using SignupCustomerRequest object
        final CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        customerEntity.setPassword(signupCustomerRequest.getPassword());

        customerService.saveCustomer(customerEntity);

        SignupCustomerResponse customerResponse = new SignupCustomerResponse()
                .id(customerEntity.getUuid())
                .status("CUSTOMER SUCCESSFULLY REGISTERED");

        return new ResponseEntity<SignupCustomerResponse>(customerResponse, HttpStatus.CREATED);
    }

    /**
     * RestController method called when the request pattern is of type '/customer/login'
     * and the incoming request is of 'POST' type
     * Login customer if valid credentials are provided and generate JWT auth token
     *
     * @param authorization - String representing the contact number and password of the customer
     * @return - ResponseEntity (LoginResponse along with HTTP status code)
     * @throws AuthenticationFailedException - if the contact number/ password provided is incorrect
     */
    @RequestMapping(method = RequestMethod.POST, path = "/customer/login",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization)
            throws AuthenticationFailedException {

        if(!authorization.substring(0, 6).equals("Basic ")) {
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }

        byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");

        if(decodedArray.length != 2) {
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }

        CustomerAuthEntity authEntity = customerService.authenticate(decodedArray[0], decodedArray[1]);
        CustomerEntity customer = authEntity.getCustomer();

        LoginResponse loginResponse = new LoginResponse()
                .id(customer.getUuid())
                .message("LOGGED IN SUCCESSFULLY")
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .emailAddress(customer.getEmail())
                .contactNumber(customer.getContactNumber());
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", authEntity.getAccessToken());
        List<String> header = new ArrayList<>();
        header.add("access-token");
        headers.setAccessControlExposeHeaders(header);

        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
    }

    /**
     * RestController method called when the request pattern is of type '/customer/logout'
     * and the incoming request is of 'POST' type
     * Sign out customer if valid authorization token is provided
     *
     * @param authorization - String represents authorization token
     * @return - ResponseEntity (LogoutResponse along with HTTP status code)
     * @throws AuthorizationFailedException - if invalid/ expired token is provided
     */
    @RequestMapping(method = RequestMethod.POST, path = "/customer/logout",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout (@RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {

        String accessToken = authorization.split("Bearer ")[1];
        CustomerAuthEntity authEntity = customerService.logout(accessToken);
        CustomerEntity customerEntity = authEntity.getCustomer();

        LogoutResponse logoutResponse = new LogoutResponse()
                .id(customerEntity.getUuid())
                .message("LOGGED OUT SUCCESSFULLY");

        return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
    }

}
