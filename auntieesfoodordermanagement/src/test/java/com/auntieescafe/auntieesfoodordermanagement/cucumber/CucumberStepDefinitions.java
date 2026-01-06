package com.auntieescafe.auntieesfoodordermanagement.cucumber;

import com.auntieescafe.auntieesfoodordermanagement.entity.User;
import com.auntieescafe.auntieesfoodordermanagement.repository.UserRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.Matchers.equalTo;

public class CucumberStepDefinitions {

    @LocalServerPort
    private int port;

    private Response response;
    private String token;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Given("the application is running")
    public void the_application_is_running() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Given("a user with role {string} is authenticated")
    public void a_user_with_role_is_authenticated(String role) {
        User user = new User();
        user.setEmail(role.toLowerCase() + "@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(role);
        user.setEmailVerified(true);
        userRepository.save(user);

        token = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\\"email\\":\\"" + user.getEmail() + "\\", \\"password\\":\\"password\\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .extract()
                .path("token");
    }

    @Given("a customer exists with name {string} and email {string}")
    public void a_customer_exists_with_name_and_email(String name, String email) {
        User customer = new User();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPassword(passwordEncoder.encode("password"));
        customer.setRole("CUSTOMER");
        customer.setEmailVerified(true);
        userRepository.save(customer);
    }

    @When("a GET request is sent to {string} with query {string}")
    public void a_get_request_is_sent_to_with_query(String path, String query) {
        response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(path + "?q=" + query);
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("the response body should contain a user with email {string}")
    public void the_response_body_should_contain_a_user_with_email(String email) {
        response.then().body("[0].email", equalTo(email));
    }
}
