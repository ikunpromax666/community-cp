package com.community.communitycp.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.communitycp.doctor.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    /**
     * 新增一条评价后原子重算医生评分。
     * <p>
     * 必须一条 SQL 完成：先算 rating_avg（用的是旧 rating_count），
     * 再把 rating_count +1。MySQL 的 SET 从左到右执行，顺序不能反。
     *
     * @param userId 医生用户ID（doctor.user_id）
     * @param score  本次评分 1~5
     */
    @Update("UPDATE doctor SET " +
            "rating_avg = (rating_avg * rating_count + #{score}) / (rating_count + 1), " +
            "rating_count = rating_count + 1 " +
            "WHERE user_id = #{userId}")
    int updateRating(@Param("userId") Long userId, @Param("score") Integer score);
}
