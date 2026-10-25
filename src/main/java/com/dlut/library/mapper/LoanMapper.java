package com.dlut.library.mapper;

import com.dlut.library.model.Loan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoanMapper {

    int insert(Loan loan);

    List<Loan> findByReader(String readerUsername);

    List<Loan> findAll();

    Loan findById(Long id);

    int countActiveForBook(@Param("bookId") Long bookId,
                           @Param("readerUsername") String readerUsername);

    int cancel(@Param("id") Long id,
               @Param("readerUsername") String readerUsername);

    int confirmPickup(Long id);

    int markReturned(@Param("id") Long id,
                     @Param("readerUsername") String readerUsername,
                     @Param("librarian") boolean librarian);
}
