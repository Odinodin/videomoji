node_modules:
	npm install

shadow: node_modules
	npx shadow-cljs watch app

shadow-dev: node_modules
	npx shadow-cljs run repl/start

shadow-build: node_modules
	npx shadow-cljs compile app

building: shadow-build
	npx shadow-cljs run build/css-release
