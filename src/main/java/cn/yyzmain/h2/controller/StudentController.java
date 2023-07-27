package cn.yyzmain.h2.controller;

import cn.yyzmain.h2.entity.Student;
import cn.yyzmain.h2.mapper.StudentMapper;
import cn.yyzmain.result.MainResult;
import cn.yyzmain.result.MainResultGenerator;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student")
@Api(tags = "测试")
@Slf4j
@RequiredArgsConstructor
public class StudentController {

    private final StudentMapper studentMapper;

    @ApiOperation("列表")
    @PostMapping("/list")
    public MainResult<String> list() {
        final List<Student> students = studentMapper.selectAll();
        return MainResultGenerator.createOkResult(new Gson().toJson(students));
    }

    @ApiOperation("添加")
    @PostMapping("/add")
    public MainResult<String> add(@RequestBody Student student) {
        final int insert = studentMapper.insert(student);
        final List<Student> students = studentMapper.selectAll();
        return MainResultGenerator.createOkResult(new Gson().toJson(students));
    }

}
