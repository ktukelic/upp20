package org.ftn.upp.lass.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ftn.upp.lass.common.api.RestApiEndpoints;
import org.ftn.upp.lass.exception.BadRequestException;
import org.ftn.upp.lass.exception.BadRequestResponseCode;
import org.ftn.upp.lass.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(RestApiEndpoints.AUTH)
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtils jwtTokenUtils;
    private final JwtUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @PostMapping(RestApiEndpoints.LOG_IN)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public JwtResponse createAuthenticationToken(@Valid @RequestBody JwtRequest authenticationRequest) {
        this.authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final JwtUser userDetails = this.userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final var user = this.userRepository.findUserByUsername(authenticationRequest.getUsername());
        if (!user.get().getIsVerified()) {
            throw new BadRequestException(BadRequestResponseCode.INVALID_REQUEST_DATA, "User account not verified");
        }

        return new JwtResponse(this.jwtTokenUtils.generateToken(userDetails));
    }

    private void authenticate(String username, String password) {
        try {
            this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            throw new BadCredentialsException("Credentials invalid.");
        }
    }
}