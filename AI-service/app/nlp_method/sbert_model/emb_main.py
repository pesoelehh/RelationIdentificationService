"""
SBERT Embedding Service

This module initializes the Sentence-BERT (SBERT) model used throughout the
application for semantic similarity calculations.

The loaded multilingual model converts text into dense vector embeddings that
capture semantic meaning, enabling similarity comparisons across designations
and definitions.

Functions:
- embed_sbert(): Generates embeddings for a list of input texts.

Model:
- sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2

Author: Gabriel Nathanael da Gomez
Email: gabrieldagomez@gmail.com
"""

from sentence_transformers import SentenceTransformer

# SBERT MODEL
# Load the multilingual Sentence-BERT model once during application startup.
# The model is reused across requests to avoid repeated loading overhead.
sbert_model = SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")

def embed_sbert(texts):
    """
    Generate SBERT embeddings for a list of texts.

    This function converts input texts into dense semantic vectors that can
    be used for similarity calculations, clustering, search, or downstream
    NLP tasks.

    Args:
        texts (list[str]):
            Collection of text strings to encode.

    Returns:
        numpy.ndarray:
            Embedding matrix with shape
            (number_of_texts, embedding_dimension).
    """
    # Encode input texts into semantic vector representations.
    embeddings = sbert_model.encode(texts)
    return embeddings
