package com.example.goodreads.controller;

import com.example.goodreads.model.Book;
import com.example.goodreads.service.BookService;
//import com.example.goodreads.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

//    @Autowired
//    private CommentService commentService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

//    @GetMapping("/")
//    @PreAuthorize("hasRole('client_user')")
//    public List<Book> listAllBooks() {
//        return bookService.getAllBooks();
//    }
//
//    @GetMapping("/search")
//    @PreAuthorize("hasRole('client_user')")
//    public List<Book> searchBooksByTitle(@RequestParam("title") String title) {
//        return bookService.findBooksByTitle(title);
//    }
//
//    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('client_user')")
//    public Book getBookById(@PathVariable("id") int id) {
//        return bookService.findBookById(id);
//    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse> listAllBooks() {
        if (!hasClientRole()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("Brak uprawnień do wykonania tej operacji"));
        }

        List<Book> books = bookService.getAllBooks();

        if (books.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse("Brak książek w bazie"));
        }

        return ResponseEntity.ok(new ApiResponse("Lista książek", books));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchBooksByTitle(@RequestParam("title") String title) {
        if (!hasClientRole()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("Brak uprawnień do wykonania tej operacji"));
        }

        List<Book> books = bookService.findBooksByTitle(title);

        if (books.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse("Brak książek pasujących do podanego tytułu"));
        }

        return ResponseEntity.ok(new ApiResponse("Znalezione książki", books));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBookById(@PathVariable("id") int id) {
        if (!hasClientRole()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("Brak uprawnień do wykonania tej operacji"));
        }

        Book book = bookService.findBookById(id);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Książka o podanym ID nie została znaleziona"));
        }

        return ResponseEntity.ok(new ApiResponse("Sukces", book));
    }


    @PostMapping("/")
    public ResponseEntity<ApiResponse> addBook(@Valid @RequestBody Book book, BindingResult bindingResult) {
        if (!hasClientAdminRole()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("Brak uprawnień do wykonania tej operacji"));
        }

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Niepoprawne dane: " + errors));
        }

        try {
            Book savedBook = bookService.addBook(book);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Książka została pomyślnie dodana: " + savedBook.getTitle()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Wystąpił błąd podczas dodawania książki: " + ex.getMessage()));
        }
    }

    private boolean hasClientRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_client_user"));
    }


    private boolean hasClientAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_client_admin"));
    }



}