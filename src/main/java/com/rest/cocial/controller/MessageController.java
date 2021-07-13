package com.rest.cocial.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.rest.cocial.domain.Message;
import com.rest.cocial.domain.Views;
import com.rest.cocial.repo.MessageRepo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:4200/","https://social-club-vchat.herokuapp.com/"})
@RequestMapping("/v1/message")
public class MessageController {
    private final MessageRepo messageRepo;

    @Autowired
    public MessageController(MessageRepo messageRepo) {
        this.messageRepo = messageRepo;
    }


    @GetMapping
    @JsonView(Views.IdName.class)
    public List<Message> list(){
        return messageRepo.findAll();
    }

    @GetMapping("{id}")
    @JsonView(Views.FullMessage.class)
    public Message getOne(@PathVariable("id") Message message){
        return message;
    }

    @PostMapping
    public Message create(@RequestBody Message message){
        message.setCreationDate(LocalDateTime.now());
        return messageRepo.save(message);
    }

    @PutMapping("{id}")
    public Message update(
            @PathVariable("id") Message messageFromDb,
            @RequestBody Message message){
        BeanUtils.copyProperties(message, messageFromDb, "id");

        return messageRepo.save(messageFromDb);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Message message){
        messageRepo.delete(message);
    }
}
