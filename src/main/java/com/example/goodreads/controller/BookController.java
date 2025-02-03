package com.example.goodreads.controller;

import com.example.goodreads.model.Book;
import com.example.goodreads.model.Comment;
import com.example.goodreads.service.BookNotFoundException;
import com.example.goodreads.service.BookService;
import com.example.goodreads.service.CommentNotFoundException;
import com.example.goodreads.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private CommentService commentService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }


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

//    @PostMapping("/")
//    public ResponseEntity<ApiResponse> addBook(@Valid @RequestBody Book book, BindingResult bindingResult) {
//        if (!hasClientAdminRole()) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(new ApiResponse("Brak uprawnień do wykonania tej operacji"));
//        }
//
//        ResponseEntity<ApiResponse> errors = getApiResponseResponseEntity(bindingResult);
//        if (errors != null) return errors;
//
//        try {
//            Book savedBook = bookService.addBook(book);
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body(new ApiResponse("Książka została pomyślnie dodana: " + savedBook.getTitle()));
//        } catch (Exception ex) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse("Wystąpił błąd podczas dodawania książki: " + ex.getMessage()));
//        }
//    }

//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse> updateBook(@PathVariable Long id, @Valid @RequestBody Book book, BindingResult bindingResult) {
//        if (!hasClientAdminRole()) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(new ApiResponse("Brak uprawnień do wykonania tej operacji"));
//        }
//
//        ResponseEntity<ApiResponse> errors = getApiResponseResponseEntity(bindingResult);
//        if (errors != null) return errors;
//
//        try {
//            Book updatedBook = bookService.updateBook(id, book);
//            return ResponseEntity.ok(new ApiResponse("Książka została pomyślnie zaktualizowana: " + updatedBook.getTitle()));
//        } catch (EntityNotFoundException ex) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ApiResponse("Nie znaleziono książki o podanym ID"));
//        } catch (Exception ex) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse("Wystąpił błąd podczas aktualizacji książki: " + ex.getMessage()));
//        }
//    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse> deleteBook(@PathVariable Long id) {
//        if (!hasClientAdminRole()) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(new ApiResponse("Brak uprawnień do wykonania tej operacji"));
//        }
//
//        try {
//            boolean deleted = bookService.deleteBook(id);
//            if (deleted) {
//                return ResponseEntity.status(HttpStatus.OK)
//                        .body(new ApiResponse("Książka została pomyślnie usunięta."));
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(new ApiResponse("Nie znaleziono książki o podanym ID."));
//            }
//        } catch (Exception ex) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse("Wystąpił błąd podczas usuwania książki: " + ex.getMessage()));
//        }
//    }


    @PostMapping("/{bookId}/addComment")
    public ResponseEntity<ApiResponse> addComment(@PathVariable("bookId") Long bookId,
                                                  @RequestBody @Valid Comment comment,
                                                  BindingResult bindingResult) {

        if (!hasClientRole()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("Brak uprawnień do wykonania tej operacji"));
        }

        ResponseEntity<ApiResponse> errors = getApiResponseResponseEntity(bindingResult);
        if (errors != null) return errors;


        try {
            commentService.addComment(bookId, comment);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Komentarz został dodany do książki o ID: " + bookId));
        } catch (BookNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Wystąpił błąd podczas dodawania komentarza: " + ex.getMessage()));
        }
    }

    private ResponseEntity<ApiResponse> getApiResponseResponseEntity(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Niepoprawne dane: " + errors));
        }
        return null;
    }

//    @DeleteMapping("/{bookId}/deleteComment/{commentId}")
//    public ResponseEntity<ApiResponse> deleteComment(@PathVariable("bookId") Long bookId,
//                                                     @PathVariable("commentId") Long commentId) {
//
//        if (!hasClientAdminRole()) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(new ApiResponse("Brak uprawnień do wykonania tej operacji"));
//        }
//
//        try {
//            commentService.deleteComment(bookId, commentId);
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ApiResponse("Komentarz o ID: " + commentId + " został usunięty z książki o ID: " + bookId));
//        } catch (CommentNotFoundException ex) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ApiResponse(ex.getMessage()));
//        } catch (Exception ex) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse("Wystąpił błąd podczas usuwania komentarza: " + ex.getMessage()));
//        }
//    }





    private boolean hasClientRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_client_user"));
    }
//
//    @GetMapping("/ratings")
//    public List<Object[]> getBooksWithRatings() {
//        return bookService.getBooksWithTotalRatings();
//    }
//
//    @GetMapping("/top3-commented")
//    public List<Object[]> getTop3MostCommentedBooks() {
//        return bookService.getTop3MostCommentedBooks();
//    }
//
//    @GetMapping("/average-readed")
//    public ResponseEntity<List<Object[]>> getBooksWithStatistics() {
//        List<Object[]> booksWithStatistics = bookService.getBooksWithReadCountAndAverageRating();
//        return ResponseEntity.ok(booksWithStatistics);
//    }
//
//    @GetMapping("/average-comment-length")
//    public List<Object[]> getAverageCommentLengthPerBook() {
//        return bookService.getAverageCommentLengthPerBook();
//    }



}