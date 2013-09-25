(ns recipes.playground.template)

(let [[el attr? & body] '(a { :href "http://over.the.re" } "A link")]
  [el attr? body])

(apply str "<" 'a ">" (apply str [1 2 3]) "</" 'a ">") 

(defn html
  [expr]
  (let [[el attr & body] expr
        [attr body] (if (nil? body) [{} attr] [attr body])]
    (apply str "<" el (apply str (map (fn [[k v]] (str " " (name k) "=\"" v "\"")) attr)) ">"
               (if (coll? body) (apply str (map html body)) body)
           		 "</" el ">")))

(html '(em (a "hello")))
(html '(a {:href "the.re"} (em "heyaaa")))