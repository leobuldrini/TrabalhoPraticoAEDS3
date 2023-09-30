import csv

def convert_csv_delimiter(input_file, output_file):
    with open(input_file, 'r', newline='', encoding='utf-8') as infile:
        with open(output_file, 'w', newline='', encoding='utf-8') as outfile:
            reader = csv.reader(infile)
            writer = csv.writer(outfile, delimiter=';')
            
            for row in reader:
                writer.writerow(row)

# Exemplo de uso:
convert_csv_delimiter('breaches.csv', 'arquivo_convertido.csv')
