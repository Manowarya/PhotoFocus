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
		tone FLOAT NOT NULL,
		saturation FLOAT NOT NULL, 
		bright FLOAT NOT NULL,
		exposition FLOAT NOT NULL,
		contrast FLOAT NOT NULL,
		blur FLOAT NOT NULL,
		noise FLOAT NOT NULL,
		vignette FLOAT NOT NULL,
		FOREIGN KEY (user_id) REFERENCES users (id)
	);
    `

	_, err = db.Exec(templateSql)
	if err != nil {
		panic(err)
	}
}
