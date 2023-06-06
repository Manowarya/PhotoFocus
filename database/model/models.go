package model

import (
	"database/sql"
	"github.com/labstack/echo/v4"
	"net/http"
)

type User struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type Template struct {
	Name       string  `json:"name"`
	UserId     int64   `json:"user_id"`
	Tone       float32 `json:"tone"`
	Saturation float32 `json:"saturation"`
	Bright     float32 `json:"bright"`
	Exposition float32 `json:"exposition"`
	Contrast   float32 `json:"contrast"`
	Blur       float32 `json:"blur"`
	Noise      float32 `json:"noise"`
	Vignette   float32 `json:"vignette"`
}

type Templates struct {
	Templates []Template `json:"templates"`
}

func GetTemplates(db *sql.DB, userId string) (Templates, error) {
	templates := Templates{}

	if len(userId) <= 0 {
		return templates, nil
	}

	rows, err := db.Query("SELECT name, tone, saturation, bright, exposition, contrast, blur, noise, vignette  FROM templates WHERE user_id=?", userId)

	defer rows.Close()

	for rows.Next() {
		template := Template{}
		err = rows.Scan(
			&template.Name,
			&template.Tone,
			&template.Saturation,
			&template.Bright,
			&template.Exposition,
			&template.Contrast,
			&template.Blur,
			&template.Noise,
			&template.Vignette)
		if err != nil {
			return templates, err
		}

		templates.Templates = append(templates.Templates, template)
	}

	return templates, err
}

func SaveTemplate(db *sql.DB, c echo.Context) error {
	template := new(Template)
	if err := c.Bind(template); err != nil {
		return err
	}

	insertQuery := "INSERT INTO templates (name, user_id, tone, saturation, bright, exposition, contrast, blur, noise, vignette) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
	_, err := db.Exec(insertQuery, template.Name, template.UserId, template.Tone, template.Saturation, template.Bright, template.Exposition, template.Contrast, template.Blur, template.Noise, template.Vignette)

	return err
}
func CreateUser(c echo.Context) error {
	user := new(User)
	if err := c.Bind(user); err != nil {
		return err
	}

	createdUser := &User{
		Password: user.Password,
	}

	return c.JSON(http.StatusCreated, createdUser)
}
