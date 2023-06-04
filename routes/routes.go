package routes

import (
	"GoProject/database/model"
	"database/sql"
	"fmt"
	"github.com/labstack/echo"
	"golang.org/x/crypto/bcrypt"
	"math/rand"
	"net/http"
	"net/smtp"
	"strconv"
	"time"
)

var emails = make(map[string]TemporaryСode)

type UserWithCode struct {
	Email string `json:"email"`
	Code  string `json:"code"`
}

type TemporaryСode struct {
	password string
	code     string
	time     time.Time
}

type smtpServer struct {
	host string
	port string
}

// Address URI to smtp server
func (s *smtpServer) Address() string {
	return s.host + ":" + s.port
}

func GetTemplate(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		prices, err := model.GetTemplates(db, "1")
		if err != nil {
			return c.JSON(http.StatusBadGateway, err)
		}
		return c.JSON(http.StatusOK, prices)
	}
}

func SaveTemplate(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		if err := model.SaveTemplate(db, c); err != nil {
			return c.JSON(http.StatusBadGateway, "Ошибка сервера, попробуйте позже")

		}
		return c.JSON(http.StatusCreated, "Шаблон успешно создан")
	}
}

func VerificationEmail(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		user := new(model.User)
		if err := c.Bind(user); err != nil {
			return c.JSON(http.StatusBadRequest, err)
		}

		if !IsUniqueEmail(db, user.Email) {
			return c.JSON(http.StatusConflict, "Пользователь с такой почтой уже существует")
		}

		hashedPassword := HashPassword(user.Password)
		user.Password = hashedPassword

		verificationCode := GenerateVerificationCode()

		if err := SendVerificationCode(verificationCode, user.Email); err != nil {
			return c.JSON(http.StatusInternalServerError, err)
		}

		fmt.Println(len(user.Password))
		fmt.Println(user.Email)

		emails[user.Email] = TemporaryСode{user.Password, verificationCode, time.Now()}

		fmt.Println(emails[user.Email].code)
		fmt.Println(emails[user.Email].time)
		fmt.Println(len(emails))

		return c.JSON(http.StatusOK, "Code sent successfully")
	}
}

func RegisterUser(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		user := new(UserWithCode)
		if err := c.Bind(user); err != nil {
			return c.JSON(http.StatusBadRequest, err)
		}

		if emails[user.Email].code == user.Code {
			insertQuery := "INSERT INTO users (email, password) VALUES (?, ?)"
			result, err := db.Exec(insertQuery, user.Email, emails[user.Email].password)
			if err != nil {
				fmt.Printf("Failed to insert user into database: %v\n", err)
				return c.JSON(http.StatusInternalServerError, err)
			}
			delete(emails, user.Email)
			userID, _ := result.LastInsertId()
			return c.JSON(http.StatusCreated, userID)
		}

		return c.JSON(http.StatusRequestTimeout, "Неверный код, попробуйте заново")
	}
}

func AuthorizationUser(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		user := new(model.User)
		if err := c.Bind(user); err != nil {
			return c.JSON(http.StatusBadRequest, err)
		}

		query := "SELECT id password FROM users WHERE email = ?"
		var id int
		var hashPassword string
		if err := db.QueryRow(query, user.Email).Scan(&id, &hashPassword); err != nil {
			return c.JSON(http.StatusUnauthorized, err)
		}

		if err := bcrypt.CompareHashAndPassword([]byte(hashPassword), []byte(user.Password)); err != nil {
			return c.JSON(http.StatusConflict, err)
		}

		return c.JSON(http.StatusOK, id)
	}
}

func IsUniqueEmail(db *sql.DB, email string) bool {
	query := "SELECT id FROM users WHERE email = ?"
	var id int
	err := db.QueryRow(query, email).Scan(&id)
	if err != nil {
		if err == sql.ErrNoRows {
			return true
		}
		fmt.Printf("Failed to query database: %v\n", err)
		return false
	}
	return false
}

func HashPassword(password string) string {
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		fmt.Printf("Failed to hash password: %v\n", err)
		return ""
	}

	return string(hashedPassword)
}

func GenerateVerificationCode() string {
	// Generate a random 6-digit code
	rand.Seed(time.Now().UnixNano())
	code := rand.Intn(900000) + 100000
	return strconv.Itoa(code)
}

func SendVerificationCode(verificationCode string, email string) error {
	from := "karasev_s_e@sc.vsu.ru"
	password := "assassin123and123"
	// Receiver email address.
	to := []string{email}
	// smtp server configuration.
	smtpServer := smtpServer{host: "smtp.gmail.com", port: "587"}
	// Message.
	message := []byte("Введите данный код для подтверждения почты " + verificationCode)
	// Authentication.
	auth := smtp.PlainAuth("", from, password, smtpServer.host)
	// Sending email.
	err := smtp.SendMail(smtpServer.Address(), auth, from, to, message)
	return err
}
