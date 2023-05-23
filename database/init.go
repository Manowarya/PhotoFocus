package database

import (
	"database/sql"
	_ "github.com/go-sql-driver/mysql"
)

func Initialize() *sql.DB {
	db, err := sql.Open("mysql", "root:assassin123and123@/photofocusdb")

	if err != nil {
		panic("Error connecting to database")
	}

	return db
}

func Migrate(db *sql.DB) {
	userSql := `
        CREATE TABLE IF NOT EXISTS users(
			id INT PRIMARY KEY AUTO_INCREMENT,
			email VARCHAR(50) UNIQUE NOT NULL,
			password VARCHAR(70) NOT NULL
        );
    `
	_, err := db.Exec(userSql)
	if err != nil {
		panic(err)
	}

	templateSql := `
    CREATE TABLE IF NOT EXISTS templates(
		id INT PRIMARY KEY AUTO_INCREMENT,
		user_id INT NOT NULL,
		text VARCHAR(100) NOT NULL, 
		font_size INT NOT NULL,
		text_color VARCHAR(30) NOT NULL,
		font VARCHAR(30) NOT NULL,
		light DOUBLE NOT NULL,
		bokeh DOUBLE NOT NULL, 
		color DOUBLE NOT NULL,
		grain DOUBLE NOT NULL,
		vignette DOUBLE NOT NULL,
		FOREIGN KEY (user_id) REFERENCES users (id)
	);
    `

	_, err = db.Exec(templateSql)
	if err != nil {
		panic(err)
	}
}
