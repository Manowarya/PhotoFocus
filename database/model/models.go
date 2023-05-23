package model

import (
	"database/sql"
	"github.com/labstack/echo"
	"net/http"
)

type User struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type Template struct {
	ID        int64   `json:"id"`
	UserId    int64   `json:"user_id"`
	Text      string  `json:"text"`
	FontSize  int64   `json:"font_size"`
	TextColor string  `json:"text_color"`
	Font      string  `json:"font"`
	Light     float32 `json:"light"`
	Bokeh     float32 `json:"bokeh"`
	Color     float32 `json:"color"`
	Grain     float32 `json:"grain"`
	Vignette  float32 `json:"vignette"`
}

type Templates struct {
	Templates []Template `json:"items"`
}

func GetTemplates(db *sql.DB, userId string) (Templates, error) {
	templates := Templates{}

	if len(userId) <= 0 {
		return templates, nil
	}

	rows, err := db.Query("SELECT * FROM templates WHERE user_id=?", userId)

	defer rows.Close()

	for rows.Next() {
		template := Template{}
		err = rows.Scan(
			&template.ID,
			&template.UserId,
			&template.Text,
			&template.FontSize,
			&template.TextColor,
			&template.Font,
			&template.Light,
			&template.Bokeh,
			&template.Color,
			&template.Grain,
			&template.Vignette)
		if err != nil {
			return templates, err
		}

		templates.Templates = append(templates.Templates, template)
	}

	return templates, err
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
