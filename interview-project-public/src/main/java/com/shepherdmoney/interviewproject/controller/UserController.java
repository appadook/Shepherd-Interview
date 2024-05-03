package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    // TODO: wire in the user repository (~ 1 line)
    @Autowired
    private UserRepository userRepository;

    /**
     * Creates a new user based on the provided payload.
     * @param payload the payload containing the user's name and email, not null.
     * @return ResponseEntity with the newly created user's ID and HTTP status 200 (OK).
     * @throws IllegalArgumentException if the payload is null, ensuring that the method does not proceed with invalid input.
     */
    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        // TODO: Create an user entity with information given in the payload, store it in the database
        //       and return the id of the user in 200 OK response
        User newUser = new User();
        newUser.setName(payload.getName());
        newUser.setEmail(payload.getEmail());
        userRepository.save(newUser);
        return ResponseEntity.ok(newUser.getId());
    }

    /**
     * Deletes a user identified by the provided user ID.
     * This method deletes a user from the database based on the user ID provided as a parameter.
     * If the user exists, it is deleted, and the method returns a success message. If no user is found
     * with the given ID, it returns a BadRequest status indicating the user was not found.
     * @param userId the ID of the user to be deleted.
     * @return ResponseEntity containing a success message(OK) if the user is found and deleted,
     *         or a BadRequest status with an error message if the user does not exist.
     */
    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        // TODO: Return 200 OK if a user with the given ID exists, and the deletion is successful
        //       Return 400 Bad Request if a user with the ID does not exist
        //       The response body could be anything you consider appropriate
        return userRepository.findById(userId)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok("User deleted successfully");
                })
                .orElseGet(() -> ResponseEntity.badRequest().body("User not found"));
    }
}
