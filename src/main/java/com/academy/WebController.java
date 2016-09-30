package com.academy;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.ModelAndView;


import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Controller
public class WebController {
    @Autowired
    DBRepository dBRepository;

    @RequestMapping(method = RequestMethod.GET, path = "/login")
    public ModelAndView goToLoginPage(HttpSession httpSession) {
        ModelAndView modelAndView = new ModelAndView("login");
        String loginMessage = (String) httpSession.getAttribute("loginAttempt");
        modelAndView.addObject("loginMessage", loginMessage);
        return modelAndView;
    }

    @GetMapping("/adduser")
    public ModelAndView goToAddUserPage(HttpSession httpSession) {
        ModelAndView modelAndView = new ModelAndView("adduser");
        String message = (String) httpSession.getAttribute("addUserMessage");
        modelAndView.addObject("addUserMessage", message);
        return modelAndView;
    }

    @PostMapping("/adduser")
    public String createUser(@RequestParam String name, @RequestParam String username,
                             @RequestParam String password1,@RequestParam String password2, HttpSession httpSession) {
        if (!password1.equals(password2)) {
            httpSession.setAttribute("addUserMessage","Passwords did not match. Try again");
            return "redirect:./adduser";
        }
        if (!dBRepository.validUserName(username)) {
            httpSession.setAttribute("addUserMessage","Username " + username + " already exists. Try again.");
            return "redirect:./adduser";
        }
        dBRepository.addUser(name,username,password1);
        User user = dBRepository.getUser(username);
        httpSession.setAttribute("user",user);
        return "redirect:./lists";
    }

    @PostMapping("/login")
    public String login(HttpSession httpSession, @RequestParam String username, @RequestParam String password) {
        boolean validLogin = dBRepository.isPasswordValid(username, password);
        User user = dBRepository.getUser(username);
        httpSession.setAttribute("user", user);
        httpSession.setAttribute("loginAttempt", validLogin ? null : "Login failed. Please try again.");
        return validLogin ? "redirect:./lists" : "redirect:./login";
    }

    @GetMapping("/lists")
    public ModelAndView viewLists(HttpSession httpSession) {
        ModelAndView modelAndView = new ModelAndView("lists");
        User user = (User) httpSession.getAttribute("user");
        modelAndView.addObject("user", user);
        List<LinkList> lists = dBRepository.getLists(user.getUserID());
        modelAndView.addObject("lists", lists);
        return modelAndView;
    }


    @RequestMapping(method = RequestMethod.GET, path = "/lists/{listID}")
    public ModelAndView viewLinks(HttpSession httpSession, @PathVariable Long listID) {
        List<Link> linkList = dBRepository.getLinks(listID);
        httpSession.setAttribute("listID", listID);
        User user = (User) httpSession.getAttribute("user");
        String userName = user.getUsername();
        return new ModelAndView("linkList").addObject("linkList", linkList).addObject("user", userName);
    }
    @RequestMapping(method=RequestMethod.GET, path="/createList")
    public ModelAndView viewCreateList(HttpSession httpSession){
        return new ModelAndView("newList").addObject("user", httpSession.getAttribute("user"));
    }
    @RequestMapping(method=RequestMethod.POST, path="/createList")
    public String createList(HttpSession httpSession, @RequestParam String listName, @RequestParam String description){
        User user= (User) httpSession.getAttribute("user");
        dBRepository.createNewList(user.getUserID(), listName, description);
        return "redirect:./lists";
    }

@RequestMapping(method=RequestMethod.POST, path="/addLink")
public String addLink( HttpSession httpSession, @RequestParam String url, @RequestParam String linkName, @RequestParam String description, @RequestParam boolean favorite){
   Long listID=(Long) httpSession.getAttribute("listID");
    Link link=new Link(url, favorite, listID, linkName,description);
    dBRepository.addLink(link);
    String returnAdr="redirect:./lists/"+listID;
    return returnAdr;
}

    @RequestMapping(value = "/dbtest", produces = "text/plain")
    @ResponseBody
    public String testDb() throws SQLException {
        int two = dBRepository.db();
        return "Database connectivity seems " + (two == 2 ? "OK" : "Not OK");

    }
}
