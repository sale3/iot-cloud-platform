package etf.iot.cloud.platform.services.controllers;

import etf.iot.cloud.platform.services.services.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import etf.iot.cloud.platform.services.util.Constants;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final Base64.Decoder decoder = Base64.getDecoder();
    private final AuthService authService;

    @Value("${api.key}")
    private String apiKey;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public ResponseEntity<String> login(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        //parsing credentials from Authorization header
        String[] tokens = auth.split(" ");
        byte[] data = tokens[1].getBytes();
        byte[] decodedData = decoder.decode(data);
        String credentials = new String(decodedData);
        tokens = credentials.split(":");
        String username = tokens[0];
        String password = tokens[1];
        System.out.println(Constants.ANSI_BLUE+"Device: "+username+" - Sign in request!"+Constants.ANSI_RESET);
        //authenticate device
        String jwt = authService.login(username, password);
        if (jwt != null){
            System.out.println(Constants.ANSI_GREEN+"Device: "+username+" - Successful sign in!"+Constants.ANSI_RESET);
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        }
        else{
            System.out.println(Constants.ANSI_RED+"Device: "+username+" - Sign in failed"+Constants.ANSI_RESET);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    // used for checking whether jwt has expired
    @GetMapping("/jwt-check")
    public ResponseEntity<String> checkJwt() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> register(@RequestHeader(HttpHeaders.AUTHORIZATION) String key, @RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("time_format") String time_format) {
        // allow signup only for devices that have valid api key
        System.out.println(Constants.ANSI_BLUE+"Device: "+username+" - Sign up request!"+Constants.ANSI_RESET);
        if (apiKey.equals(key)) {
            String jwt = authService.register(username, password, time_format);
            if (jwt != null)
                return new ResponseEntity<>(jwt, HttpStatus.OK);
        }
        else
            System.out.println(Constants.ANSI_RED+"Device: "+username+" - Invalid API key!"+Constants.ANSI_RESET);
        System.out.println(Constants.ANSI_RED+"Device: "+username+" - Sign up failed!"+Constants.ANSI_RESET);
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}
