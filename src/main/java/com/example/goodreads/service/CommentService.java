package com.example.goodreads.service;

import com.example.goodreads.model.Book;
import com.example.goodreads.model.Comment;
import com.example.goodreads.repository.BookRepository;
import com.example.goodreads.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookRepository bookRepository;

    public List<Comment> findCommentsByBookId(int bookId) {
        return commentRepository.findByBookId(bookId);
    }

    public void saveComment(Comment comment) {
        commentRepository.save(comment);
    }

    public void addComment(Long bookId, Comment comment) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));
        comment.setBook(book);
        commentRepository.save(comment);
    }

    public void deleteComment(Long bookId, Long commentId) throws CommentNotFoundException {
        if (!bookRepository.existsById(bookId)) {
            throw new CommentNotFoundException("Książka o podanym ID nie istnieje");
        }
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException("Komentarz o podanym ID nie istnieje");
        }
        commentRepository.deleteById(commentId);
    }

    public Optional<Comment> findById(Long commentId) {
        return commentRepository.findById(commentId);
    }
}


