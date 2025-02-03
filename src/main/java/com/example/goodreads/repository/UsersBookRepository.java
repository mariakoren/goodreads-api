package com.example.goodreads.repository;

import com.example.goodreads.model.UsersBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersBookRepository extends JpaRepository<UsersBook, Long> {

    // Wyszukaj książki po nazwie użytkownika
    List<UsersBook> findByUsername(String username);

    // Wyszukaj książki po nazwie użytkownika i statusie
    List<UsersBook> findByUsernameAndStatus(String username, UsersBook.Status status);

    // Wyszukaj książki po książce, jeśli chcesz móc filtrować po książkach
    List<UsersBook> findByBook_Id(Long bookId);
}
