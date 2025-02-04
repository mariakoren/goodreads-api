package com.example.goodreads.controller;

import com.example.goodreads.model.Book;
import com.example.goodreads.model.Comment;
import com.example.goodreads.service.BookService;
import com.example.goodreads.service.CommentNotFoundException;
import com.example.goodreads.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@PreAuthorize("hasRole('client_admin')")
public class AdminController {

    private final BookService bookService;

    @Autowired
    private CommentService commentService;

    public AdminController(BookService bookService) {
        this.bookService = bookService;
    }


    @PostMapping("/")
    public ResponseEntity<ApiResponse> addBook(@Valid @RequestBody Book book, BindingResult bindingResult) {

        ResponseEntity<ApiResponse> errors = getApiResponseResponseEntity(bindingResult);
        if (errors != null) return errors;

        try {
            Book savedBook = bookService.addBook(book);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Książka została pomyślnie dodana: " + savedBook.getTitle()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Wystąpił błąd podczas dodawania książki: " + ex.getMessage()));
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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateBook(@PathVariable Long id, @Valid @RequestBody Book book, BindingResult bindingResult) {

        ResponseEntity<ApiResponse> errors = getApiResponseResponseEntity(bindingResult);
        if (errors != null) return errors;

        try {
            Book updatedBook = bookService.updateBook(id, book);
            return ResponseEntity.ok(new ApiResponse("Książka została pomyślnie zaktualizowana: " + updatedBook.getTitle()));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Nie znaleziono książki o podanym ID"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Wystąpił błąd podczas aktualizacji książki: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBook(@PathVariable Long id) {
        try {
            boolean deleted = bookService.deleteBook(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse("Książka została pomyślnie usunięta."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse("Nie znaleziono książki o podanym ID."));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Wystąpił błąd podczas usuwania książki: " + ex.getMessage()));
        }
    }

    @DeleteMapping("/{bookId}/deleteComment/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable("bookId") Long bookId,
                                                     @PathVariable("commentId") Long commentId) {

        try {
            commentService.deleteComment(bookId, commentId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse("Komentarz o ID: " + commentId + " został usunięty z książki o ID: " + bookId));
        } catch (CommentNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Wystąpił błąd podczas usuwania komentarza: " + ex.getMessage()));
        }
    }

    @PutMapping("/{bookId}/editComment/{commentId}")
    public ResponseEntity<ApiResponse> editComment(@PathVariable("bookId") Long bookId,
                                                   @PathVariable("commentId") Long commentId,
                                                   @RequestBody @Valid Comment comment,
                                                   BindingResult bindingResult) {


        if (bindingResult.hasErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> errorMessages.append(error.getDefaultMessage()).append("\n"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Błędy walidacji: " + errorMessages.toString()));
        }


        if (!bookService.existsById(bookId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Książka o podanym ID nie istnieje"));
        }


        Optional<Comment> existingComment = commentService.findById(commentId);
        if (existingComment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Komentarz o podanym ID nie istnieje"));
        }

        // Aktualizacja komentarza
        try {
            Comment updatedComment = existingComment.get();
            updatedComment.setContent(comment.getContent());
            updatedComment.setRating(comment.getRating());

            commentService.saveComment(updatedComment);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse("Komentarz został zaktualizowany."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Wystąpił błąd podczas edytowania komentarza: " + ex.getMessage()));
        }
    }

    // STATYSTYKI


    @GetMapping("/ratings")
    public ResponseEntity<List<Object[]>> getBooksWithRatings() {
        List<Object[]> booksWithRatings = bookService.getBooksWithTotalRatings();
        return booksWithRatings.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(booksWithRatings);
    }

    @GetMapping("/top3-commented")
    public ResponseEntity<List<Object[]>> getTop3MostCommentedBooks() {
        List<Object[]> topCommentedBooks = bookService.getTop3MostCommentedBooks();
        return topCommentedBooks.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(topCommentedBooks);
    }

    @GetMapping("/average-readed")
    public ResponseEntity<List<Object[]>> getBooksWithStatistics() {
        List<Object[]> booksWithStatistics = bookService.getBooksWithReadCountAndAverageRating();
        return booksWithStatistics.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(booksWithStatistics);
    }

    @GetMapping("/average-comment-length")
    public ResponseEntity<List<Object[]>> getAverageCommentLengthPerBook() {
        List<Object[]> averageCommentLength = bookService.getAverageCommentLengthPerBook();
        return averageCommentLength.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(averageCommentLength);
    }

    @GetMapping("/readers-count")
    public ResponseEntity<List<Object[]>> getBooksWithReadersCount() {
        List<Object[]> booksWithReaders = bookService.getBooksWithReadersCount();
        return booksWithReaders.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(booksWithReaders);
    }



}
