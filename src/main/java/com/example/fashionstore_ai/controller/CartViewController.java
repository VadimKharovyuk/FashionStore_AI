package com.example.fashionstore_ai.controller;
import com.example.fashionstore_ai.config.SessionResolver;
import com.example.fashionstore_ai.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/cart")
public class CartViewController {

    private final CartService cartService;
    private final SessionResolver sessionResolver;

    @GetMapping()
    public String cartPage() {


        return "cart";
    }
}
