const fs = require('fs');
const path = require('path');

// Recupera i percorsi dei file dal terminale
const inputFilePath = process.argv[2];
const outputFilePath = process.argv[3];

// Verifica che i percorsi siano stati forniti
if (!inputFilePath || !outputFilePath) {
  console.error('Usage: node script.js <inputFilePath> <outputFilePath>');
  process.exit(1);
}

// Specifica il percorso del file openapi.json
const inputFilePathComplete = path.join(inputFilePath);
const outputFilePathComplete = path.join(outputFilePath);


// Lista dei tag da mantenere (puoi modificare questi tag o passare dinamicamente come argomento del comando)
const tagsToRemove = ["support", "external-v2", "contract", "internal-v1","support-pnpg", "external-pnpg"];

// Leggi il file openapi.json
fs.readFile(inputFilePathComplete, 'utf8', (err, data) => {
    if (err) {
        console.error(`Errore durante la lettura del file: ${err}`);
        return;
    }

    let openapi;
    try {
        openapi = JSON.parse(data);
    } catch (err) {
        console.error(`Errore durante il parsing del JSON: ${err}`);
        return;
    }

    // Filtra i tag nel file openapi
    if (openapi.tags && Array.isArray(openapi.tags)) {
        openapi.tags = openapi.tags.filter(tag => !tagsToRemove.includes(tag.name));
    }

    // Rimuovi v* dal path
    if (openapi.paths && typeof openapi.paths === 'object') {
        Object.keys(openapi.paths).forEach(path => {
            if(path.startsWith('/v')) {
                const pathKeyWithoutVersion = path.substring(3);
                const pathItem = openapi.paths[path];
                
                delete openapi.paths[path];
                openapi.paths[pathKeyWithoutVersion] = pathItem;
            }
           
        });
    }

    // Filtra i tag nei paths
    if (openapi.paths && typeof openapi.paths === 'object') {
        Object.keys(openapi.paths).forEach(path => {
            const pathItem = openapi.paths[path];
            ['get', 'post', 'put', 'delete', 'options', 'head', 'patch', 'trace'].forEach(method => {
                if (pathItem[method] && Array.isArray(pathItem[method].tags)) {
                    pathItem[method].tags = pathItem[method].tags.filter(tag => !tagsToRemove.includes(tag));
                }
            });
        });
    }

    // Salva il file modificato
    fs.writeFile(outputFilePath, JSON.stringify(openapi, null, 2), 'utf8', err => {
        if (err) {
            console.error(`Errore durante il salvataggio del file: ${err}`);
            return;
        }

        console.log(`File salvato con successo in ${outputFilePathComplete}`);
    });

     // Elimina il file
     fs.unlink(inputFilePathComplete, err => {
        if (err) {
            console.error(`Errore durante l'eliminazione del file: ${err}`);
            return;
        }

        console.log(`File eliminato con successo: ${inputFilePathComplete}`);
    });
});
