package Controller;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import Service.ExceptionSer;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class SocialMediaController {
    private final AccountService accountService;
    private final MessageService messageService;

    public SocialMediaController() {
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.post("/register", this::rA);
        app.post("/login", this::lA);
        app.post("/messages", this::cM);
        app.get("/messages", this::gAM);
        app.get("/messages/{message_id}", this::gMI);
        app.delete("/messages/{message_id}", this::dMI);
        app.patch("/messages/{message_id}", this::uMI);
        app.get("/accounts/{account_id}/messages",
                this::gMAI);

        return app;

    }

    private void rA(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(ctx.body(), Account.class);
        try {
            Account registeredAccount = accountService.createAccount(account);
            ctx.json(mapper.writeValueAsString(registeredAccount));
        } catch (ExceptionSer e) {
            ctx.status(400);
        }
    }

    
    private void lA(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(); 
        Account account = mapper.readValue(ctx.body(), Account.class);

        try {
            Optional<Account> loggedInAccount = accountService
                    .validateLogin(account);
            if (loggedInAccount.isPresent()) {
                ctx.json(mapper.writeValueAsString(loggedInAccount));
                ctx.sessionAttribute("logged_in_account",
                        loggedInAccount.get());
                ctx.json(loggedInAccount.get());
            } else {
                ctx.status(401);
            }
        } catch (ExceptionSer e) {
            ctx.status(401);
        }
    }

    
    private void cM(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);
        try {
            Optional<Account> account = accountService
                    .getAccountById(mappedMessage.getPosted_by());
            Message message = messageService.createMessage(mappedMessage,
                    account);
            ctx.json(message);
        } catch (ExceptionSer e) {
            ctx.status(400);
        }
    }

    private void gAM(Context ctx) {

        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages);
    }

    private void gMI(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                ctx.json(message.get());
            } else {
                
                ctx.status(200); 
                ctx.result(""); 
            }
            
        } catch (NumberFormatException e) {
            ctx.status(400); 
        } catch (ExceptionSer e) {
            ctx.status(200); 
            ctx.result("");
        }
    }

    
    private void dMI(Context ctx) {
        try {
            
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                messageService.deleteMessage(message.get());
                ctx.status(200);
                ctx.json(message.get());
            } else {
                ctx.status(200);
            }
        } catch (ExceptionSer e) {
            ctx.status(200);
        }
    }

    private void uMI(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            mappedMessage.setMessage_id(id);
            Message messageUpdated = messageService
                    .updateMessage(mappedMessage);

            ctx.json(messageUpdated);

        } catch (ExceptionSer e) {
            ctx.status(400);
        }
    }

    private void gMAI(Context ctx) {
        try {
            int accountId = Integer.parseInt(ctx.pathParam("account_id"));
            List<Message> messages = messageService
                    .getMessagesByAccountId(accountId);
            if (!messages.isEmpty()) {
                ctx.json(messages);
            } else {
                ctx.json(messages);
                ctx.status(200);
            }
        } catch (ExceptionSer e) {
            ctx.status(400);
        }
    }
}