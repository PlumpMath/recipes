all: hello-clojure

db_file=recipes.sqlite3
sql=sqlite3 ${db_file}

hello-clojure:
	@echo 'Hello, Clojure!'

repl:
	lein repl

clean-db:
	${sql} 'delete from ingredientsinrecipes'
	${sql} 'delete from ingredients'
	${sql} 'delete from recipes'

setup-db:
	${sql} 'create table recipes (id integer primary key, title text, description text);'
	${sql} 'create table ingredients (id integer primary key, name text);'
	${sql} 'create table ingredientsinrecipes (recipes_id integer references recipes(id), ingredients_id integer references ingredients(id), quantity text)'
