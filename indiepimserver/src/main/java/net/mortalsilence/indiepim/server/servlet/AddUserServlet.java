package net.mortalsilence.indiepim.server.servlet;

import javax.servlet.http.HttpServlet;

/**
 * Created with IntelliJ IDEA.
 * User: AmIEvil
 * Date: 16.01.13
 * Time: 20:18
 * To change this template use File | Settings | File Templates.
 */
// FIXME convert to spring MVC controller
public class AddUserServlet extends HttpServlet {

//    final static Logger logger = Logger.getLogger("net.mortalsilence.indiepim");
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        final String username = request.getParameter("username");
//        final String password = request.getParameter("password");
//        response.setContentType("text/html");
//
//        if(username == null) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            response.getWriter().print("<html><body>Parameter 'username' not given</body></html>");
//            return;
//        }
//
//        if(password == null) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            response.getWriter().print("<html><body>Parameter 'password' not given</body></html>");
//            return;
//        }
//
//
//        final UserService loginService = new UserService(userDAO, genericDAO);
//        try {
//            loginService.addUser(username, password);
//            response.setStatus(HttpServletResponse.SC_OK);
//        } catch (Exception e) {
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            response.getWriter().print(e.getMessage());
//            logger.error("Error adding new user: " + username, e);
//        }
//    }
}
