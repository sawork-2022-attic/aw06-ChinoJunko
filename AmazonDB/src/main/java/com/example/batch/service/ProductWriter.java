package com.example.batch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import com.example.batch.model.Product;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ProductWriter implements ItemWriter<Product>, StepExecutionListener {


    private JdbcTemplate jdbcTemplate;

    public ProductWriter(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("jdbcconfig.xml");
        jdbcTemplate = (JdbcTemplate) ac.getBean("jdbcTemplate");
//        String initsql = "drop table if exists product;\n" +
//                "create table product(main_cat text,"+
//                "title text,asin varchar(20),"+
//                "category text,imageURLHighRes text," +
//                "primary key(asin));";
//        jdbcTemplate.execute(initsql);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    @Override
    public void write(List<? extends Product> list) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String addsql = "replace into product values(?,?,?,?,?)";
        list.stream().forEach(product -> {
            String category="",imageURLHighRes="";
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                objectMapper.writeValue(outputStream, product.getCategory());
                category = outputStream.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                objectMapper.writeValue(outputStream, product.getImageURLHighRes());
                imageURLHighRes = outputStream.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Object[] args = {product.getMain_cat(), product.getTitle(), product.getAsin(), category, imageURLHighRes};
            jdbcTemplate.update(addsql,args);
        });
    }
}
