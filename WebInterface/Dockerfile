# Unified Docker Image for AI MultiBarcode Capture
# Contains Apache+PHP, MySQL, and phpMyAdmin in a single container

FROM ubuntu:22.04

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Minimum php8.1 package release that contains the PHP2Shell fix (see RUN check below)
ARG PHP81_MIN_VERSION=8.1.2-1ubuntu2.28

# Install all required packages
RUN apt-get update && apt-get install -y \
    apache2 \
    php8.1 \
    php8.1-mysql \
    libapache2-mod-php8.1 \
    php8.1-curl \
    php8.1-cli \
    php8.1-mbstring \
    php8.1-xml \
    php8.1-zip \
    php8.1-gd \
    php8.1-intl \
    unzip \
    git \
    mysql-server \
    mysql-client \
    wget \
    curl \
    supervisor \
    nano \
    && rm -rf /var/lib/apt/lists/*

# PHP2Shell (CVE-2026-109618 / CVE-2026-109619): Ubuntu jammy ships the fix in
# php8.1 8.1.2-1ubuntu2.28 (jammy-security). Fail the build if apt installed
# anything older, so a stale mirror or cached layer can't ship a vulnerable mod_php.
RUN dpkg-query -W -f='${db:Status-Status}\t${Package}\t${Version}\n' 'php8.1*' 'libapache2-mod-php8.1' \
    | awk -F'\t' '$1=="installed"{print $2"\t"$3}' \
    | while IFS="$(printf '\t')" read -r pkg ver; do \
        echo "$pkg $ver"; \
        dpkg --compare-versions "$ver" ge "$PHP81_MIN_VERSION" \
          || { echo "ERROR: $pkg $ver is older than $PHP81_MIN_VERSION (PHP2Shell CVE-2026-109618/CVE-2026-109619 unfixed)" >&2; exit 1; }; \
      done

# Mitigation only (not the fix): disable session upload progress, as recommended
# by the PHP team for PHP2Shell. Applied to every SAPI (apache2, cli) via phpenmod.
RUN printf '; PHP2Shell (CVE-2026-109618 / CVE-2026-109619) mitigation, not the fix\nsession.upload_progress.enabled = 0\n' \
      > /etc/php/8.1/mods-available/php2shell-mitigation.ini \
    && phpenmod -v 8.1 php2shell-mitigation

# Install Composer for PHP dependency management
RUN curl -sS https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin --filename=composer

# Install phpMyAdmin
RUN wget https://files.phpmyadmin.net/phpMyAdmin/5.2.1/phpMyAdmin-5.2.1-all-languages.tar.gz \
    && tar xzf phpMyAdmin-5.2.1-all-languages.tar.gz \
    && mv phpMyAdmin-5.2.1-all-languages /var/www/html/phpmyadmin \
    && rm phpMyAdmin-5.2.1-all-languages.tar.gz

# Configure Apache
RUN a2enmod rewrite headers expires env ssl
COPY apache/000-default.conf /etc/apache2/sites-available/000-default.conf
COPY apache/ssl-default.conf /etc/apache2/sites-available/ssl-default.conf
RUN a2ensite ssl-default

# Configure phpMyAdmin
COPY phpmyadmin/config.inc.php /var/www/html/phpmyadmin/config.inc.php

# Configure MySQL
COPY mysql/my.cnf /etc/mysql/conf.d/custom.cnf

# Copy application source code
COPY src/ /var/www/html/

# Copy composer.json and install dependencies
COPY composer.json /var/www/html/composer.json
RUN cd /var/www/html && composer install --no-dev --optimize-autoloader --no-interaction || echo "No composer dependencies to install"

# Copy database initialization script
COPY database/init.sql /tmp/init.sql

# Create necessary directories first
RUN mkdir -p /scripts

# Copy startup script and fix line endings
COPY startup.sh /startup.sh
RUN sed -i 's/\r$//' /startup.sh && chmod +x /startup.sh

# Create necessary directories and set permissions
RUN mkdir -p /var/run/mysqld \
    && mkdir -p /var/log/supervisor \
    && mkdir -p /var/log/mysql \
    && mkdir -p /var/log/apache2 \
    && mkdir -p /scripts \
    && chown mysql:mysql /var/run/mysqld \
    && chown mysql:mysql /var/lib/mysql \
    && chown -R www-data:www-data /var/www/html \
    && chmod -R 755 /var/www/html

# Initialize MySQL data directory (if not already done)
RUN service mysql start && sleep 5 && service mysql stop || true

# Environment variables with defaults
ENV MYSQL_ROOT_PASSWORD=
ENV MYSQL_DATABASE=barcode_wms
ENV MYSQL_USER=wms_user
ENV MYSQL_PASSWORD=wms_password

# Expose ports (will be dynamic based on WEB_PORT)
EXPOSE 3500 3543

# Start the services
CMD ["/startup.sh"]