CURDIR := $(shell pwd)
export COMPOSE_PROJECT_PATH=$(CURDIR)

all: build


build:
	@mkdir -p $(CURDIR)/volumes/postgres $(CURDIR)/volumes/redis $(CURDIR)/volumes/minio
	@docker compose -f docker-compose.yml up -d --build

start:
	@docker compose -f docker-compose.yml up -d

stop:
	@docker compose -f docker-compose.yml stop

clean:
	@docker compose -f docker-compose.yml down -v

fclean:
	@docker compose -f docker-compose.yml down -v
	@docker system prune -af
	@rm -rf $(CURDIR)/volumes/

re: clean build
restart: stop start
