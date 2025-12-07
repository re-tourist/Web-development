package com.example.quiz.mapper;

import com.example.quiz.model.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO users (userName, userPassword, isDelete, userRole, createTime, updateTime) " +
            "VALUES (#{userName}, #{userPassword}, #{isDelete}, #{userRole}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET isDelete=1, updateTime=NOW() WHERE id=#{id} AND isDelete=0")
    int logicalDelete(@Param("id") Long id);

    @Update("""
            <script>
            UPDATE users
            <set>
                <if test='userName != null'>userName=#{userName},</if>
                <if test='userPassword != null'>userPassword=#{userPassword},</if>
                <if test='userRole != null'>userRole=#{userRole},</if>
                updateTime=NOW()
            </set>
            WHERE id=#{id} AND isDelete=0
            </script>
            """)
    int update(User user);

    @Select("""
            <script>
            SELECT * FROM users WHERE isDelete=0
            <if test='username != null and username != ""'> AND userName LIKE CONCAT('%',#{username},'%')</if>
            <if test='from != null'> AND createTime &gt;= #{from} </if>
            <if test='to != null'> AND createTime &lt;= #{to} </if>
            ORDER BY createTime DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<User> query(@Param("username") String username,
                     @Param("from") Date from,
                     @Param("to") Date to,
                     @Param("offset") int offset,
                     @Param("pageSize") int pageSize);

    @Select("""
            <script>
            SELECT COUNT(*) FROM users WHERE isDelete=0
            <if test='username != null and username != ""'> AND userName LIKE CONCAT('%',#{username},'%')</if>
            <if test='from != null'> AND createTime &gt;= #{from} </if>
            <if test='to != null'> AND createTime &lt;= #{to} </if>
            </script>
            """)
    int count(@Param("username") String username,
              @Param("from") Date from,
              @Param("to") Date to);

    @Select("SELECT * FROM users WHERE userName=#{username} AND isDelete=0 LIMIT 1")
    User findByUsername(@Param("username") String username);

    @Select("SELECT * FROM users WHERE id=#{id} LIMIT 1")
    User findById(@Param("id") Long id);
}
