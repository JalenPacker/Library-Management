// Library.java
import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Main.java
import java.util.ArrayList;
import java.util.Scanner;

class Book {
    private String title;
    private String author;
    private String category;
    private String isbn;
    private boolean isCheckedOut;
    private String dueDate;

    public Book(String title, String author, String category, String isbn) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.isCheckedOut = false;
        this.dueDate = "";
    }

    // Getters and setters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getIsbn() { return isbn; }
    public boolean isCheckedOut() { return isCheckedOut; }
    public String getDueDate() { return dueDate; }

    public void checkOut(String dueDate) {
        this.isCheckedOut = true;
        this.dueDate = dueDate;
    }

    public void checkIn() {
        this.isCheckedOut = false;
        this.dueDate = "";
    }

    @Override
    public String toString() {
        return String.format("Title: %s, Author: %s, Category: %s, ISBN: %s, Status: %s%s",
                title, author, category, isbn,
                isCheckedOut ? "Checked Out" : "Available",
                isCheckedOut ? ", Due: " + dueDate : "");
    }
}

class Library {
    private ArrayList<Book> books;
    private final String DATABASE_FILE = "library_database.txt";

    public Library() {
        books = new ArrayList<>();
        loadBooksFromFile();
    }

    // Recursive search method
    public ArrayList<Book> searchBooksByCategory(String category, int index) {
        // Base case: if index is beyond the list size, return empty ArrayList
        if (index >= books.size()) {
            return new ArrayList<>();
        }

        // Recursive case
        ArrayList<Book> result = searchBooksByCategory(category, index + 1);
        if (books.get(index).getCategory().equalsIgnoreCase(category)) {
            result.add(books.get(index));
        }
        return result;
    }

    // Exception handling for file operations
    private void loadBooksFromFile() {
        try {
            File file = new File(DATABASE_FILE);
            if (!file.exists()) {
                file.createNewFile();
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    Book book = new Book(parts[0], parts[1], parts[2], parts[3]);
                    if (parts.length > 4 && parts[4].equals("true")) {
                        book.checkOut(parts[5]);
                    }
                    books.add(book);
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error loading books: " + e.getMessage());
        }
    }

    private void saveBooksToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(DATABASE_FILE));
            for (Book book : books) {
                writer.write(String.format("%s|%s|%s|%s|%s|%s\n",
                        book.getTitle(), book.getAuthor(), book.getCategory(),
                        book.getIsbn(), book.isCheckedOut(), book.getDueDate()));
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error saving books: " + e.getMessage());
        }
    }

    public void addBook(String title, String author, String category, String isbn) {
        books.add(new Book(title, author, category, isbn));
        saveBooksToFile();
    }

    public boolean removeBook(String isbn) {
        for (Iterator<Book> iterator = books.iterator(); iterator.hasNext();) {
            Book book = iterator.next();
            if (book.getIsbn().equals(isbn)) {
                iterator.remove();
                saveBooksToFile();
                return true;
            }
        }
        return false;
    }

    public Book findBook(String isbn) {
        for (Book book : books) {
            if (book.getIsbn().equals(isbn)) {
                return book;
            }
        }
        return null;
    }

    public void checkOutBook(String isbn) {
        Book book = findBook(isbn);
        if (book != null && !book.isCheckedOut()) {
            LocalDate dueDate = LocalDate.now().plusDays(14);
            book.checkOut(dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            saveBooksToFile();
        }
    }

    public void checkInBook(String isbn) {
        Book book = findBook(isbn);
        if (book != null && book.isCheckedOut()) {
            book.checkIn();
            saveBooksToFile();
        }
    }

    public void displayAllBooks() {
        // Loop for displaying books
        for (Book book : books) {
            System.out.println(book);
        }
    }

    public ArrayList<Book> getBooks() {
        return books;
    }
}

public class LibraryManagement {
    private static Library library;
    private static Scanner scanner;

    public static void main(String[] args) {
        library = new Library();
        scanner = new Scanner(System.in);

        while (true) {
            displayMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    removeBook();
                    break;
                case 3:
                    searchByCategory();
                    break;
                case 4:
                    checkOutBook();
                    break;
                case 5:
                    checkInBook();
                    break;
                case 6:
                    library.displayAllBooks();
                    break;
                case 7:
                    System.out.println("Goodbye!");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\n=== Library Management System ===");
        System.out.println("1. Add Book");
        System.out.println("2. Remove Book");
        System.out.println("3. Search by Category");
        System.out.println("4. Check Out Book");
        System.out.println("5. Check In Book");
        System.out.println("6. Display All Books");
        System.out.println("7. Exit");
    }

    private static void addBook() {
        System.out.println("\n=== Add New Book ===");
        String title = getStringInput("Enter title: ");
        String author = getStringInput("Enter author: ");
        String category = getStringInput("Enter category: ");
        String isbn = getStringInput("Enter ISBN: ");
        
        library.addBook(title, author, category, isbn);
        System.out.println("Book added successfully!");
    }

    private static void removeBook() {
        String isbn = getStringInput("Enter ISBN of book to remove: ");
        if (library.removeBook(isbn)) {
            System.out.println("Book removed successfully!");
        } else {
            System.out.println("Book not found.");
        }
    }

    private static void searchByCategory() {
        String category = getStringInput("Enter category to search: ");
        ArrayList<Book> foundBooks = library.searchBooksByCategory(category, 0);
        
        if (foundBooks.isEmpty()) {
            System.out.println("No books found in this category.");
        } else {
            System.out.println("\nBooks in category '" + category + "':");
            for (Book book : foundBooks) {
                System.out.println(book);
            }
        }
    }

    private static void checkOutBook() {
        String isbn = getStringInput("Enter ISBN of book to check out: ");
        Book book = library.findBook(isbn);
        
        if (book == null) {
            System.out.println("Book not found.");
        } else if (book.isCheckedOut()) {
            System.out.println("Book is already checked out.");
        } else {
            library.checkOutBook(isbn);
            System.out.println("Book checked out successfully!");
        }
    }

    private static void checkInBook() {
        String isbn = getStringInput("Enter ISBN of book to check in: ");
        Book book = library.findBook(isbn);
        
        if (book == null) {
            System.out.println("Book not found.");
        } else if (!book.isCheckedOut()) {
            System.out.println("Book is not checked out.");
        } else {
            library.checkInBook(isbn);
            System.out.println("Book checked in successfully!");
        }
    }

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
