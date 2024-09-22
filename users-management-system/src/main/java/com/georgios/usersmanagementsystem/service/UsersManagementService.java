package com.georgios.usersmanagementsystem.service;

import com.georgios.usersmanagementsystem.dto.ReqRes;
import com.georgios.usersmanagementsystem.entity.OurUsers;
import com.georgios.usersmanagementsystem.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class UsersManagementService {

    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    PasswordEncoder passwordEncoder;

    public ReqRes register(ReqRes registrationRequest) {
        ReqRes resp = new ReqRes();

        try {
            OurUsers ourUser = new OurUsers();
            ourUser.setEmail(registrationRequest.getEmail());
            ourUser.setCity(registrationRequest.getCity());
            ourUser.setRole(registrationRequest.getRole());
            ourUser.setName(registrationRequest.getName());
            ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            OurUsers ourUsersResult = usersRepo.save(ourUser);
            if (ourUsersResult.getId() > 0) {
                resp.setOurUsers(ourUsersResult);
                resp.setMessage("User Saved Succesfully");
                resp.setStatusCode(200);
            }
        } catch (Exception e) {
            resp.setStatusCode(500);
            resp.setError(e.getMessage());
        }
        return resp;
    }

    public ReqRes login(ReqRes loginRequest) {
        ReqRes response = new ReqRes();
        try {
            authenticationManager.
                    authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                            loginRequest.getPassword()));
            var user = usersRepo.findByEmail(loginRequest.getEmail()).orElseThrow();
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole());
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24 Hours");
            response.setMessage("Succesfully Logged In");
        } catch (Exception ex) {
            response.setStatusCode(500);
            response.setMessage(ex.getMessage());
        }
        return response;
    }

    public ReqRes refreshToken(ReqRes refreshTokenRequest) {
        ReqRes response = new ReqRes();
        try {
            String ourEmail = jwtUtils.extractUsername(refreshTokenRequest.getToken());
            OurUsers users = usersRepo.findByEmail(ourEmail).orElseThrow();
            if (jwtUtils.isTokenValid(refreshTokenRequest.getToken(), users)) {
                var jwt = jwtUtils.generateToken(users);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenRequest.getToken());
                response.setExpirationTime("24 Hours");
                response.setMessage("Succesfully RefreshedToken");
            }
            response.setStatusCode(200);
            return response;
        } catch (Exception ex) {
            response.setStatusCode(500);
            response.setMessage(ex.getMessage());
        }
        return response;
    }

    public ReqRes getAllUsers() {
        ReqRes reqRes = new ReqRes();
        try {
            List<OurUsers> result = usersRepo.findAll();
            if (!result.isEmpty()) {
                reqRes.setOurUsersList(result);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Successful");
            }
            return reqRes;
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage(e.getMessage());
            return reqRes;
        }
    }

    public ReqRes getUserById(Integer id) {
        ReqRes reqRes = new ReqRes();
        try {
            OurUsers userById = usersRepo.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
            reqRes.setOurUsers(userById);
            reqRes.setStatusCode(200);
            reqRes.setMessage("User with id" + id + " found successfully");
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage(e.getMessage());
        }
        return reqRes;
    }

    public ReqRes deleteUser(Integer userId) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> userOptional = usersRepo.findById(userId);
            if(userOptional.isPresent()){
                usersRepo.deleteById(userId);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User with id" + userId + " deleted successfully");
            }else{
                reqRes.setStatusCode(500);
                reqRes.setMessage("User Not Found For Deletion");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(404);
            reqRes.setMessage(e.getMessage());
        }
        return reqRes;
    }

    public ReqRes updateUser(Integer userId, OurUsers updateUser) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> userOptional = usersRepo.findById(userId);
            if(userOptional.isPresent()){
                OurUsers existingUser = userOptional.get();
                existingUser.setEmail(updateUser.getEmail());
                existingUser.setName(updateUser.getName());
                existingUser.setCity(updateUser.getCity());
                existingUser.setRole(updateUser.getRole());

                if(updateUser.getPassword() != null && !updateUser.getPassword().isEmpty()){
                    existingUser.setPassword(passwordEncoder.encode(updateUser.getPassword()));
                }

                OurUsers savedUser = usersRepo.save(existingUser);
                reqRes.setOurUsers(savedUser);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User with id updated successfully");
            }else{
                reqRes.setStatusCode(500);
                reqRes.setMessage("User Not Found For Update");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(404);
            reqRes.setMessage(e.getMessage());
        }
        return reqRes;
    }

    public ReqRes getMyInfo(String email) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> userOptional = usersRepo.findByEmail(email);
            if(userOptional.isPresent()){
                reqRes.setOurUsers(userOptional.get());
                reqRes.setStatusCode(200);
                reqRes.setMessage("Success");
            }else{
                reqRes.setStatusCode(500);
                reqRes.setMessage("Not Found");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(404);
            reqRes.setMessage(e.getMessage());
        }
        return reqRes;
    }

}
