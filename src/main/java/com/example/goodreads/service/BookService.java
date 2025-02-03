package com.example.goodreads.service;

import com.example.goodreads.model.Book;
import com.example.goodreads.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> findBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public Book findBookById(int id) {
        return bookRepository.findById(id);
    }

    public Book addBook(Book book) {
        return bookRepository.save(book);
    }


    public boolean deleteBook(Long id) {
        Optional<Book> bookOptional = bookRepository.findById(id);
        if (bookOptional.isPresent()) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Book updateBook(Long id, Book bookDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono książki o ID: " + id));

        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setDescription(bookDetails.getDescription());
        book.setGenre(bookDetails.getGenre());

        return bookRepository.save(book);
    }

    public List<Object[]> getBooksWithTotalRatings() {
        return bookRepository.findTotalRatingForBooks();
    }

    public List<Object[]> getTop3MostCommentedBooks() {
        Pageable pageable = PageRequest.of(0, 3); // Ograniczenie do 3 książek
        return bookRepository.findTop3MostCommentedBooks(pageable);
    }

}