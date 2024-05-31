#!/bin/bash

# This script is meant to be run within 'wsl' and not directly within virtual machine nor on actual linux system.
# It is intended to be used with 'start.sh' for fast system shutdown.
# It should be changed for the production, where it would target only processes of the current app.
kill -15 -1
