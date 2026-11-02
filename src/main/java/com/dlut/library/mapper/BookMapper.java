package com.dlut.library.mapper;

import com.dlut.library.model.Book;
import com.dlut.library.model.BookQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BookMapper {

    List<Book> search(BookQuery query);

    Book findById(Long id);

    int insert(Book book);

    int update(Book book);

    int deleteById(Long id);

    int decrementAvailable(Long id);

    int incrementAvailable(Long id);

    int countTitles();

    int countAvailableTitles();

    int sumCopies();

    int countActiveLoans();

    int countByCategory(@Param("category") String category);
}
