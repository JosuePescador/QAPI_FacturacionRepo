# 🚀 Guía Rápida - GitHub Actions (30 segundos)

## 3 Pasos para Activar

### 1️⃣ Agregar Secretos (2 min)
```
GitHub → Settings → Secrets → New secret
```

Agrega:
```
KEYCLOAK_URL=https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token
KEYCLOAK_CLIENT_ID=apifactmasiva
KEYCLOAK_CLIENT_SECRET=2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV
```

### 2️⃣ Commit de Archivos (1 min)
```bash
git add .github/
git commit -m "feat: add GitHub Actions workflows"
git push origin scripts
```

### 3️⃣ Verificar (inmediato)
```
GitHub → Actions → Verás los 4 workflows ejecutándose
```

---

## ¿Qué hace cada workflow?

| Workflow | Cuándo | Qué hace | Tiempo |
|----------|--------|----------|--------|
| **test.yml** | Push/PR | ✅ Compila + tests E2E | 6-8 min |
| **code-quality.yml** | Push/PR | ✅ mvn verify | 5-7 min |
| **deploy-test.yml** | Push develop | 🚀 Deploy automático | 8-10 min |
| **security.yml** | Semanal | 🔒 Análisis seguridad | 8-12 min |

---

## Comandos Útiles

```bash
# Ver logs de un workflow
gh run view <run-id> --log

# Re-ejecutar un workflow
gh run rerun <run-id>

# Cancelar ejecución
gh run cancel <run-id>

# Listar últimas ejecuciones
gh run list --limit 10
```

---

## 🔗 Links

- **Actions:** https://github.com/JosuePescador/QAPI_FacturacionRepo/actions
- **Secrets:** https://github.com/JosuePescador/QAPI_FacturacionRepo/settings/secrets/actions
- **Docs Completa:** `.github/README.md`

---

## ✅ Validar Que Funciona

1. Haz push a tu rama
2. Ve a **Actions**
3. Si ves ✅ = **ÉXITO**
4. Si ves ❌ = Revisa logs

---

## 💡 Tips

✅ Los workflows se ejecutan **automáticamente**
✅ Todos en **paralelo**
✅ Logs guardados **90 días**
✅ Sin costo adicional *(tiene límite mensual)*

---

**¿Necesitas más ayuda?** Ver `.github/CHECKLIST.md`
