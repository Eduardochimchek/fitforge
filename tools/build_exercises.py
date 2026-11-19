#!/usr/bin/env python3
"""Gera app/src/main/assets/exercises.json a partir do free-exercise-db,
das traduções (data-src/traducao/*.json) e dos extras (data-src/extras.json).

Uso: python3 tools/build_exercises.py
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "data-src"
OUT = ROOT / "app" / "src" / "main" / "assets" / "exercises.json"

SEED_VERSION = 1
IMG_BASE = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"

EQUIP = {
    None: "nenhum",
    "body only": "nenhum",
    "dumbbell": "halteres",
    "barbell": "barra",
    "e-z curl bar": "barra",
    "machine": "maquina",
    "cable": "cabo",
    "kettlebells": "kettlebell",
    "bands": "elastico",
    "medicine ball": "bola_medicinal",
    "exercise ball": "bola_suica",
    "foam roll": "rolo",
    "other": "outro",
}
HOME_EQUIP = {
    "nenhum", "halteres", "barra_fixa", "kettlebell", "elastico", "bola_medicinal",
    "bola_suica", "rolo", "banco_cadeira", "corda", "colchonete",
}
LEVEL = {"beginner": "iniciante", "intermediate": "intermediario", "expert": "avancado"}

BARRA_FIXA = re.compile(r"(pull.?up|chin|hanging|muscle_up|rocky_pull|side_to_side_chins|one_handed_hang)", re.I)
EQUIP_OVERRIDE = {
    "Bench_Dips": "banco_cadeira",
    "Rope_Jumping": "corda",
    "Dips_-_Chest_Version": "paralelas",
    "Dips_-_Triceps_Version": "paralelas",
    "Parallel_Bar_Dip": "paralelas",
    "Ring_Dips": "outro",
    "Mountain_Climbers": "nenhum",
    "Inverted_Row": "barra",
    "Ab_Roller": "outro",
}
CAT_OVERRIDE = {
    "Mountain_Climbers": "hiit",
    "Air_Bike": "calistenia",
    "Battling_Ropes": "hiit",
    "Rope_Jumping": "hiit",
    "Inchworm": "mobilidade",
    "Split_Squats": "hiit",
}
NO_HOME = re.compile(r"(treadmill|stairmaster|elliptical|stationary|step_mill|smith|sled|prowler|leverage|machine)", re.I)


def categoria(x):
    if x["id"] in CAT_OVERRIDE:
        return CAT_OVERRIDE[x["id"]]
    c = x["category"]
    if c == "stretching":
        return "mobilidade"
    if c == "plyometrics":
        return "hiit"
    if c == "cardio":
        return "cardio"
    if c == "strength" and x["equipment"] in (None, "body only"):
        return "calistenia"
    return "musculacao"


def equipamento(x):
    if x["id"] in EQUIP_OVERRIDE:
        return EQUIP_OVERRIDE[x["id"]]
    if BARRA_FIXA.search(x["id"]) and x["equipment"] in (None, "body only", "other", "bands"):
        return "barra_fixa"
    return EQUIP.get(x["equipment"], "outro")


def main():
    base = json.loads((SRC / "free-exercise-db.json").read_text(encoding="utf-8"))
    trad = {}
    for f in sorted((SRC / "traducao").glob("*.json")):
        trad.update(json.loads(f.read_text(encoding="utf-8")))
    extras_file = SRC / "extras.json"
    extras = json.loads(extras_file.read_text(encoding="utf-8")) if extras_file.exists() else []

    out = []
    for x in base:
        eq = equipamento(x)
        cat = categoria(x)
        t = trad.get(x["id"])
        casa = eq in HOME_EQUIP and not NO_HOME.search(x["id"])
        por_tempo = cat in ("cardio", "mobilidade") or x.get("force") == "static"
        out.append({
            "id": x["id"],
            "nome": t["nome"] if t else x["name"],
            "nomeEn": x["name"],
            "categoria": cat,
            "nivel": LEVEL.get(x["level"], "iniciante"),
            "equipamento": eq,
            "principais": x["primaryMuscles"],
            "secundarios": x["secondaryMuscles"],
            "casa": casa,
            "porTempo": por_tempo,
            "instrucoes": t["instrucoes"] if t else x["instructions"],
            "traduzido": bool(t),
            "imagens": [IMG_BASE + i for i in x["images"]],
            "dica": None,
        })

    for e in extras:
        out.append({
            "id": e["id"],
            "nome": e["nome"],
            "nomeEn": e["nomeEn"],
            "categoria": e["categoria"],
            "nivel": e["nivel"],
            "equipamento": e["equipamento"],
            "principais": e["musculosPrincipais"],
            "secundarios": e["musculosSecundarios"],
            "casa": e["equipamento"] in HOME_EQUIP,
            "porTempo": e["porTempo"],
            "instrucoes": e["instrucoes"],
            "traduzido": True,
            "imagens": [],
            "dica": e.get("dica"),
        })

    ids = [o["id"] for o in out]
    assert len(ids) == len(set(ids)), "id duplicado"
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps({"versao": SEED_VERSION, "exercicios": out}, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    print(f"{len(out)} exercícios ({sum(o['traduzido'] for o in out)} em português, {len(extras)} extras) -> {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
