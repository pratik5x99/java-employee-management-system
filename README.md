# Chapter 1: Java Servlets and the Web Container Architecture

## 1.1 Introduction to Servlets

A **Servlet** is a Java class that executes within a Web Container (such as Apache Tomcat) to extend the capabilities of a server. Servlets natively process incoming network requests, execute business logic, and generate dynamic responses (HTML, JSON, XML) back to the client over the HTTP protocol.

The **Web Container** is responsible for managing the network socket bindings, thread allocation, and the complete lifecycle of the Servlet objects.

---

## 1.2 The Servlet API Hierarchy

To create a Servlet, a Java class must implement the `javax.servlet.Servlet` interface. However, developers rarely implement this directly. The API provides an inheritance tree designed specifically for HTTP traffic:

* **`javax.servlet.Servlet` (Interface):** The root interface defining the lifecycle methods (`init`, `service`, `destroy`).
* **`javax.servlet.GenericServlet` (Abstract Class):** Implements the `Servlet` interface and provides protocol-independent routing.
* **`javax.servlet.http.HttpServlet` (Abstract Class):** Extends `GenericServlet` and adds HTTP-specific functionality. This is the base class for all modern web Servlets. It reads the HTTP method (GET, POST, etc.) and routes the request to the corresponding `doXXX` method.

---

## 1.3 The Servlet Lifecycle

The Web Container strictly controls the lifecycle of a Servlet. A Servlet is typically instantiated as a **Singleton**—meaning only one instance of the class exists in the server's memory, and multiple concurrent network requests are handled by spawning new threads that access this single instance.

* **Loading and Instantiation:** The Web Container loads the Servlet class and creates an instance in memory.
* **`init(ServletConfig config)`:** Executed exactly once in the Servlet's lifetime. It is used to initialize resources, load configuration files, or establish initial database connection pools.
* **`service(ServletRequest req, ServletResponse res)`:** Executed for every incoming network request. The container spawns a new thread, passes the request and response objects to this method, and `HttpServlet` routes it to `doGet`, `doPost`, etc.
* **`destroy()`:** Executed exactly once when the container shuts down or unloads the application. Used to cleanly close database connections or file I/O streams.

---

## 1.4 HTTP Service Methods

When a client issues an HTTP request, the `service()` method determines the HTTP verb and delegates the logic to specific overridden methods:

* **`doGet`:** Handles HTTP GET requests. Used strictly for data retrieval. Data is passed within the URL string. GET requests are idempotent (repeating the request yields the same server state).
* **`doPost`:** Handles HTTP POST requests. Used for state-mutating operations (inserting records, processing payments). The payload is encapsulated within the HTTP request body, invisible in the URL string.
* **`doPut` / `doDelete`:** Used for updating existing resources or removing them, typically in RESTful API architectures.

---

## 1.5 Core HTTP Objects

The Web Container parses the raw TCP/IP byte stream and constructs two Java objects, passing them into the `doXXX` methods.

### `HttpServletRequest`

This object contains all data sent by the client to the server.

* **`getParameter(String name)`:** Retrieves form data or URL query parameters as Strings.
* **`getCookies()`:** Returns an array of `Cookie` objects sent by the browser.
* **`getHeader(String name)`:** Retrieves specific HTTP headers (e.g., `User-Agent`, `Authorization`).
* **`getSession()`:** Retrieves the `HttpSession` associated with the request.

### `HttpServletResponse`

This object allows the server to construct the data sent back to the client.

* **`setContentType(String type)`:** Sets the MIME type of the response (e.g., `"text/html"`, `"application/json"`).
* **`getWriter()`:** Returns a `PrintWriter` to write character text (HTML/JSON) into the response body.
* **`sendRedirect(String url)`:** Commands the client browser to initiate a new GET request to a different URL.
* **`addCookie(Cookie c)`:** Appends a cookie to the HTTP response headers.

---

## 1.6 Network Routing: Forwarding vs. Redirection

There are two distinct mechanisms for passing control from one Servlet to another resource.

### 1. Request Forwarding (`RequestDispatcher.forward`)

* **Mechanism:** The transfer of control happens entirely on the server side. The Web Container passes the exact same `HttpServletRequest` and `HttpServletResponse` objects to a new Servlet or JSP.
* **URL Visibility:** The client's browser URL does not change. The browser is unaware the transfer occurred.
* **Data Persistence:** Because the request object is identical, any data stored using `req.setAttribute()` remains available.

### 2. URL Redirection (`HttpServletResponse.sendRedirect`)

* **Mechanism:** The server halts execution and sends an HTTP 302 status code to the client's browser, along with a `Location` header. The browser immediately fires a brand new, empty GET request to the new URL.
* **URL Visibility:** The client's browser URL changes to the new destination.
* **Data Persistence:** Because a new request is generated, all data in the original request (parameters and attributes) is permanently destroyed.

---

## 1.7 State Management

HTTP is a stateless protocol; the server retains no memory of previous requests. State management bridges this gap.

### Cookies

Small text files stored on the client's browser.

* **Creation:** `Cookie c = new Cookie("user", "admin"); res.addCookie(c);`
* **Characteristics:** Sent automatically by the browser on subsequent requests. Limited storage capacity. Vulnerable to client-side manipulation unless secured with `HttpOnly` and `Secure` flags.

### HttpSession

Server-side memory allocation for individual users.

* **Mechanism:** When `req.getSession()` is called, Tomcat allocates an object in heap memory and generates a cryptographically secure token (e.g., `JSESSIONID`). This token is sent to the browser as a Cookie. On the next request, the browser returns the `JSESSIONID`, allowing Tomcat to locate the correct memory block.
* **Storage:** `session.setAttribute("key", Object)` allows storing complex Java objects securely on the server.
* **Lifecycle:** Sessions are destroyed via explicit programmatic invalidation (`session.invalidate()`) or via server-configured timeouts.

---

## 1.8 Configuration and Context

Data can be initialized and shared across the application without hardcoding it.

### `ServletConfig`

Configuration parameters specific to a single Servlet.

* Historically defined in `web.xml` using `<init-param>`.
* Used to pass specific data (like an API key or an admin email) that *only* that specific Servlet requires.
* Accessed via `getServletConfig().getInitParameter("key")`.

### `ServletContext`

Configuration parameters and memory space shared globally across the entire web application.

* Available to every Servlet in the Web Container.
* Can be used to store global application state: `getServletContext().setAttribute("globalKey", Object)`.
* Often used to load global configuration files or define database driver URLs.

---

## 1.9 Annotations vs. Deployment Descriptor (`web.xml`)

Prior to Servlet 3.0 (Java EE 6), all routing and configuration were strictly defined in an XML file called `web.xml` (the Deployment Descriptor). Modern architectures utilize Java Annotations for rapid configuration.

* **`@WebServlet("/path")`:** Maps a Servlet class to a specific URL pattern. Eliminates the need for `<servlet>` and `<servlet-mapping>` XML tags.
* **`@WebFilter("/path/*")`:** Maps a Filter class to intercept traffic matching the URL pattern.
* **`@WebInitParam`:** Used within `@WebServlet` to define `ServletConfig` initialization parameters directly in the class metadata.

---

## 1.10 Middleware: Filters

A **Filter** is an object that dynamically intercepts requests and responses to transform or use the information contained in them.

* **Architecture:** Filters sit between the incoming network request and the target Servlet. They implement `javax.servlet.Filter`.
* **The `doFilter` Method:** Contains the interception logic. The filter can inspect the request (e.g., checking for an active `HttpSession`), log data, or modify the response headers.
* **The `FilterChain`:** To allow the request to proceed to the target Servlet, the filter explicitly calls `chain.doFilter(req, res)`. If this method is omitted (e.g., during an authentication failure), the request is dropped, and the target Servlet never executes.