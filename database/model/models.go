package model

import (
	"database/sql"
)

type User struct {
	ID       int64  `json:"id"`
	Email    string `json:"email"`
	Password string `json:"password"`
}

type Template struct {
	ID        int64   `json:"id"`
	UserID    int64   `json:"user_id"`
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
	Devices []Template `json:"items"`
}

func GetTemplates(db *sql.DB, userId string) (Template, error) {
	template := Template{}

	if len(userId) <= 0 {
		return template, nil
	}

	err := db.QueryRow("SELECT * FROM templates WHERE user_id=?", userId).Scan(
		&template.ID,
		&template.UserID,
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
		//return CreateSettings(db, uuid)
	}

	return template, err
}
