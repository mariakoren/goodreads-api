package com.example.goodreads.repository;

import com.example.goodreads.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.domain.Pageable;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String title);
    Book findById(long id);


    @Query("SELECT b.id, b.title, b.author, SUM(c.rating) AS totalRating " +
            "FROM Book b LEFT JOIN b.comments c " +
            "GROUP BY b.id " +
            "ORDER BY totalRating DESC")
    List<Object[]> findTotalRatingForBooks();

    @Query("SELECT b.id, b.title, b.author, COUNT(c.id) AS commentCount " +
            "FROM Book b LEFT JOIN b.comments c " +
            "GROUP BY b.id " +
            "ORDER BY commentCount DESC")
    List<Object[]> findTop3MostCommentedBooks(Pageable pageable);

}