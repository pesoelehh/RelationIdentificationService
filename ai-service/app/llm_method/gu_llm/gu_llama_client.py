from openai import OpenAI
from app.llm_method.config.settings import GU_BASE_URL, GU_API_KEY, LLM_MODEL, TEMPERATURE


class GUClient:
    """
    Thin wrapper around the OpenAI-compatible API.
    """

    def __init__(self):
        self.client = OpenAI(
            api_key=GU_API_KEY,
            base_url=GU_BASE_URL
        )

    def classify(self, prompt: str) -> str:
        """
        Sends a prompt to Model and returns raw text output.
        """
        response = self.client.chat.completions.create(
            messages=[{
                "role": "user",
                "content": [{"type": "text", "text": prompt}]
            }],
            model=LLM_MODEL,
            temperature=TEMPERATURE
        )

        return response.choices[0].message.content.strip()
