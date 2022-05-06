package com.example.webpos.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.example.webpos.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: Junko
 * @Email: imaizumikagerouzi@foxmail.com
 * @Date: 6/5/2022 上午9:48
 */
@Repository
public class Amazon implements PosDB{

    private List<Product> products = new ArrayList<>();

    private ObjectMapper objectMapper;

    @Value("${url}")
    String Url;

    @Value("${name}")
    String Username;

    @Value("${password}")
    String Password;

    @Cacheable(value = "products")
    @Override
    public List<Product> getProducts() {
        try (DruidDataSource druidDataSource = new DruidDataSource()){
            druidDataSource.setUrl(Url);
            druidDataSource.setUsername(Username);
            druidDataSource.setPassword(Password);
            druidDataSource.init();
            Connection connection = druidDataSource.getConnection(50000L);

            products.clear();
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
            }

            PreparedStatement preparedStatement = connection.prepareStatement("select * from product order by rand() limit 100");
            preparedStatement.setFetchSize(Integer.MIN_VALUE);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Product product = new Product();
                product.setMain_cat(resultSet.getString(1));
                product.setTitle(resultSet.getString(2));
                product.setAsin(resultSet.getString(3));
                product.setCategory(objectMapper.readValue(resultSet.getString(4), ArrayList.class));
                List<String> list = objectMapper.readValue(resultSet.getString(5), ArrayList.class);
                if (list.isEmpty()) {
                    product.setImage("");
                } else {
                    product.setImage(list.get(0));
                }
                product.setPrice(ThreadLocalRandom.current().nextInt(114514, 1919810));
                products.add(product);
            }
            connection.close();
            return products;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ArrayList<>();
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Product getProduct(String productAsin) {
        for (Product p : products) {
            if (p.getAsin().equals(productAsin)) {
                return p;
            }
        }
        return null;
    }

}
