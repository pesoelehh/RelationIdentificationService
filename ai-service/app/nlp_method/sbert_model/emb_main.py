from sentence_transformers import SentenceTransformer

# ===========================
# SBERT MODEL
# ===========================
sbert_model = SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")

def embed_sbert(texts):
    """
    Encode a list of texts with SBERT.

    Args:
        texts: List of strings

    Returns:
        torch.Tensor of shape (len(texts), embedding_dim)
    """
    embeddings = sbert_model.encode(texts)
    return embeddings


# ===========================
# JINA MODEL (Optional)
# ===========================
# from sentence_transformers import SentenceTransformer as JinaTransformer
# jina_model = JinaTransformer(
#     "jinaai/jina-embeddings-v2-base-de",
#     trust_remote_code=True
# )
# jina_model.max_seq_length = 1024
#
# def embed_jina(texts: List[str], normalize: bool = True) -> torch.Tensor:
#     embeddings = jina_model.encode(texts, convert_to_tensor=True)
#     if normalize:
#         embeddings = torch.nn.functional.normalize(embeddings, p=2, dim=1)
#     return embeddings


# ===========================
# GerMedBERT MODEL (Optional)
# ===========================
# from transformers import AutoTokenizer, AutoModel
#
# tokenizer = AutoTokenizer.from_pretrained("germedbert/medbert-512")
# medbert_model = AutoModel.from_pretrained("germedbert/medbert-512")
#
# def embed_medbert(texts: List[str]) -> torch.Tensor:
#     """
#     Encode texts using GerMedBERT (mean pooling of last hidden states)
#     """
#     inputs = tokenizer(
#         texts,
#         padding=True,
#         truncation=True,
#         max_length=512,
#         return_tensors="pt"
#     )
#     with torch.no_grad():
#         outputs = medbert_model(**inputs)
#         last_hidden = outputs.last_hidden_state  # (batch, seq_len, hidden_dim)
#         attention_mask = inputs.attention_mask.unsqueeze(-1)
#         sum_embeddings = torch.sum(last_hidden * attention_mask, dim=1)
#         sum_mask = torch.clamp(attention_mask.sum(dim=1), min=1e-9)
#         mean_pooled = sum_embeddings / sum_mask
#     return mean_pooled
