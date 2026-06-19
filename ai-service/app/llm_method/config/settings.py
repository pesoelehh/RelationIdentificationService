import os
from dotenv import load_dotenv

load_dotenv()
GU_API_KEY = os.getenv("LLM_API_KEY")
GU_BASE_URL = os.getenv("LLM_URL_ENDPOINT")
LLM_MODEL = "llama-3.3-70b-instruct"
TEMPERATURE = 0
