package com.example.goodreads.controller;

import com.example.goodreads.model.Book;
import com.example.goodreads.model.UsersBook;
import com.example.goodreads.repository.BookRepository;
import com.example.goodreads.repository.UsersBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users-books")
@RequiredArgsConstructor
public class UsersBookController {

    private final UsersBookRepository usersBookRepository;
    private final BookRepository bookRepository;

    @GetMapping
    public ResponseEntity<?> getUserBooks(@RequestParam(required = false) UsersBook.Status status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Użytkownik nie jest zalogowany");
        }
        String username = authentication.getName();
        List<UsersBook> books = (status != null) ?
                usersBookRepository.findByUsernameAndStatus(username, status) :
                usersBookRepository.findByUsername(username);
        return ResponseEntity.ok(books);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookStatus(@PathVariable Long id, @RequestParam String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Użytkownik nie jest zalogowany");
        }
        String username = authentication.getName();

        Optional<UsersBook> usersBookOptional = usersBookRepository.findById(id);
        if (usersBookOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Nie znaleziono książki użytkownika");
        }
        UsersBook usersBook = usersBookOptional.get();

        if (!usersBook.getUsername().equals(username)) {
            return ResponseEntity.status(403).body("Brak dostępu do edycji tej książki");
        }

        try {
            UsersBook.Status newStatus = UsersBook.Status.valueOf(status.toUpperCase());
            usersBook.setStatus(newStatus);
            usersBookRepository.save(usersBook);
            return ResponseEntity.ok(usersBook);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Niepoprawny status. Dozwolone statusy: READED, WANT_READ, UNREAD.");
        }
    }


    @PostMapping("/init")
    public ResponseEntity<?> initializeUserBooks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Użytkownik nie jest zalogowany");
        }
        String username = authentication.getName();

        List<Book> books = bookRepository.findAll();

        for (Book book : books) {
            UsersBook usersBook = new UsersBook();
            usersBook.setUsername(username);
            usersBook.setBook(book);
            usersBook.setStatus(UsersBook.Status.UNREAD);
            usersBookRepository.save(usersBook);
        }

        return ResponseEntity.ok("Wszystkie książki zostały zainicjalizowane jako 'UNREAD'");
    }
}
