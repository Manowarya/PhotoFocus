package routes

import (
	"GoProject/database/model"
	"database/sql"
	"fmt"
	"github.com/labstack/echo/v4"
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

type TemplateID struct {
	Name   string `json:"name"`
	UserId int64  `json:"user_id"`
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

// GetFont отправляет шрифт.
// @Summary GetFont
// @Description Отправляет шрифт по индефикатору
// @Tags Fonts
// @Accept  json
// @Produce  json
// @Param id path string true "Font ID"
// @Success 200 {object} model.Font
// @Failure 502 {string} string "Ошибка сервера, попробуйте позже"
// @Router /get-font/{id} [get]
func GetFont(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		id := c.Param("id")
		font, err := model.GetFont(db, id)
		if err != nil {
			return c.JSON(http.StatusBadGateway, "Ошибка сервера, попробуйте позже")
		}
		return c.JSON(http.StatusOK, font)
	}
}

// GetTemplate отправляет шаблон.
// @Summary GetTemplate
// @Description Отправляет шаблон по индефикатору пользователя
// @Tags Templates
// @Accept  json
// @Produce  json
// @Param id path string true "User ID"
// @Success 200 {object} model.Templates
// @Failure 502 {string} string "Ошибка сервера, попробуйте позже"
// @Router /get-templates/{id} [get]
func GetTemplate(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		id := c.Param("id")
		templates, err := model.GetTemplates(db, id)
		if err != nil {
			return c.JSON(http.StatusBadGateway, "Ошибка сервера, попробуйте позже")
		}
		return c.JSON(http.StatusOK, templates)
	}
}

// SaveTemplate сохраняет новый шаблон.
// @Summary SaveTemplate
// @Description Создание нового шаблона
// @Tags Templates
// @Accept  json
// @Produce  json
// @Param template body model.Template true "Template"
// @Success 201 {string} string "Шаблон успешно создан"
// @Failure 502 {string} string "Ошибка сервера, попробуйте позже"
// @Router /save-template [post]
func SaveTemplate(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		if err := model.SaveTemplate(db, c); err != nil {
			return c.JSON(http.StatusBadGateway, "Ошибка сервера, попробуйте позже")

		}
		return c.JSON(http.StatusCreated, "Шаблон успешно создан")
	}
}

// UpdateTemplate изменяет шаблон.
// @Summary UpdateTemplate
// @Description Изменение шаблона
// @Tags Templates
// @Accept  json
// @Produce  json
// @Param template body model.Template true "Template"
// @Success 201 {string} string "Шаблон изменен"
// @Failure 502 {string} string "Ошибка сервера, попробуйте позже"
// @Router /update-template [post]
func UpdateTemplate(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		if err := model.UpdateTemplate(db, c); err != nil {
			return c.JSON(http.StatusBadGateway, "Ошибка сервера, попробуйте позже")

		}
		return c.JSON(http.StatusCreated, "Шаблон изменен")
	}
}

// DeleteTemplate удаляет шаблон.
// @Summary DeleteTemplate
// @Description Удаление шаблона
// @Tags Templates
// @Accept  json
// @Produce  json
// @Param template body TemplateID true "TemplateID"
// @Success 201 {string} string "Шаблон удален"
// @Failure 502 {string} string "Ошибка сервера, попробуйте позже"
// @Router /delete-template [post]
func DeleteTemplate(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		if err := model.DeleteTemplate(db, c); err != nil {
			return c.JSON(http.StatusBadGateway, "Ошибка сервера, попробуйте позже")

		}
		return c.JSON(http.StatusCreated, "Шаблон удален")
	}
}

// VerificationEmail отправляет код подтверждения.
// @Summary VerificationEmail
// @Description Отправляет код подтверждения при помощи smtp.gmail.com на почту пользователя
// @Tags User
// @Accept  json
// @Produce  json
// @Param user body model.User true "User"
// @Success 200 {string} string "Код успешно отправлен"
// @Failure 400 {string} string "Некорректный запрос"
// @Failure 409 {string} string "Пользователь с такой почтой уже существует"
// @Failure 500 {string} string "Проверте корректность почты или попробуйте позже"
// @Router /verification [post]
func VerificationEmail(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		user := new(model.User)
		if err := c.Bind(user); err != nil {
			return c.JSON(http.StatusBadRequest, "Некорректный запрос")
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

		return c.JSON(http.StatusOK, "Код успешно отправлен")
	}
}

// RegisterUser регистрация пользователя.
// @Summary RegisterUser
// @Description После ввода кода подтверждения регистрирует пользователя
// @Tags User
// @Accept  json
// @Produce  json
// @Param user body UserWithCode true "User"
// @Success 201 {int} int userID
// @Failure 400 {string} string "Некорректный запрос"
// @Failure 408 {string} string "Неверный код, попробуйте заново"
// @Failure 500 {string} string "Ошибка сервера, попробуйте позже"
// @Router /register [post]
func RegisterUser(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		user := new(UserWithCode)
		if err := c.Bind(user); err != nil {
			return c.JSON(http.StatusBadRequest, "Некорректный запрос")
		}

		if emails[user.Email].code == user.Code {
			insertQuery := "INSERT INTO users (email, password) VALUES (?, ?)"
			result, err := db.Exec(insertQuery, user.Email, emails[user.Email].password)
			if err != nil {
				return c.JSON(http.StatusInternalServerError, "Ошибка сервера, попробуйте позже")
			}
			delete(emails, user.Email)
			userID, _ := result.LastInsertId()
			return c.JSON(http.StatusCreated, userID)
		}

		return c.JSON(http.StatusRequestTimeout, "Неверный код, попробуйте заново")
	}
}

// AuthorizationUser авторизация пользователя.
// @Summary AuthorizationUser
// @Description Авторизация пользователя
// @Tags User
// @Accept  json
// @Produce  json
// @Param user body model.User true "User"
// @Success 200 {int} int userID
// @Failure 400 {string} string "Некорректный запрос"
// @Failure 401 {string} string "Пользователя с такой почтой не существует"
// @Failure 409 {string} string "Неверный пароль"
// @Router /authorization [post]
func AuthorizationUser(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		user := new(model.User)
		if err := c.Bind(user); err != nil {
			return c.JSON(http.StatusBadRequest, "Некорректный запрос")
		}

		query := "SELECT id, password FROM users WHERE email = ?"
		var id int
		var hashPassword string
		if err := db.QueryRow(query, user.Email).Scan(&id, &hashPassword); err != nil {
			return c.JSON(http.StatusUnauthorized, "Пользователя с такой почтой не существует")
		}

		if err := bcrypt.CompareHashAndPassword([]byte(hashPassword), []byte(user.Password)); err != nil {
			return c.JSON(http.StatusConflict, "Неверный пароль")
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
