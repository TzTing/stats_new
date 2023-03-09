package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author: Tz
 * @Date: 2023/02/04 16:31
 */
public interface NoteRepository extends JpaRepository<Note, Integer>, JpaSpecificationExecutor<Note> {


    /**
     * 查询所有开启的信息提示
     * @return
     */
    @Query("FROM Note WHERE visible=1 ORDER BY disId")
    List<Note> findNotes();

}
