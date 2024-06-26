package com.game.play.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommonMapper{


    /**
     * 查询活动对应的会员产品黑名单配置
     * @return
     */
    @Select("SELECT simulator_path from simulator_paths WHERE simulator_type = #{simulatorType}")
    String querySimulatorPathByType(@Param("simulatorType") int simulatorType);


}