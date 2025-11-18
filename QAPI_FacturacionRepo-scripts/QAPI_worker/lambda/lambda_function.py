import boto3
import json

ses = boto3.client(
    'ses',
    region_name='us-east-1',
    aws_access_key_id='test',           # para LocalStack puedes dejar test/test
    aws_secret_access_key='test',
    endpoint_url='http://localhost:4566' 
)

def lambda_handler(event, context):
    print("Evento recibido:")
    print(json.dumps(event, indent=2))

    for record in event.get("Records", []):
        if record["eventName"] not in ("INSERT", "MODIFY"):
            continue

        new_image = record["dynamodb"].get("NewImage", {})
        estado = new_image.get("estado", {}).get("S", "").lower()
        tipo_servicio = new_image.get("tipoServicio", {}).get("S", "N/A")
        id_request = new_image.get("id", {}).get("S", "sin_id")

        # 🧩 Lógica condicional
        if "exito" in estado:
            subject = f"✅ Solicitud {id_request} procesada con éxito"
            message = f"El servicio {tipo_servicio} se completó correctamente."
        elif "error" in estado:
            subject = f"❌ Error en la solicitud {id_request}"
            message = f"Ocurrió un error al procesar el servicio {tipo_servicio}."
        else:
            subject = f"ℹ️ Estado actualizado: {estado}"
            message = f"La solicitud {id_request} cambió su estado a: {estado}"

        print(f"Enviando correo: {subject}")

        # 💌 Enviar correo (solo simulado si estás en LocalStack)
        ses.send_email(
            Source="no-reply@empresa.com",
            Destination={"ToAddresses": ["usuario@ejemplo.com"]},
            Message={
                "Subject": {"Data": subject},
                "Body": {"Text": {"Data": message}}
            }
        )

    return {"status": "ok"}
