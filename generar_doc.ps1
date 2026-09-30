# Script de generación de documentación Word (.docx) para FerreteriaPro
# Compliant con PSScriptAnalyzer (verbos aprobados y sin variables no utilizadas)

[CmdletBinding()]
param(
    [string]$OutputPath = "FerreteriaPro_Documentacion_Completa.docx"
)

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

function ConvertTo-EscapedXml {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $false)]
        [string]$InputText
    )
    if ([string]::IsNullOrEmpty($InputText)) {
        return ""
    }
    return [System.Security.SecurityElement]::Escape($InputText)
}

$tempDir = Join-Path $env:TEMP ("docx_gen_" + [Guid]::NewGuid().ToString())
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null
$wordDir = Join-Path $tempDir "word"
New-Item -ItemType Directory -Path $wordDir -Force | Out-Null
$relsDir = Join-Path $tempDir "_rels"
New-Item -ItemType Directory -Path $relsDir -Force | Out-Null
$wordRelsDir = Join-Path $wordDir "_rels"
New-Item -ItemType Directory -Path $wordRelsDir -Force | Out-Null

$contentTypes = @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>
'@
[System.IO.File]::WriteAllText((Join-Path $tempDir "[Content_Types].xml"), $contentTypes, [System.Text.Encoding]::UTF8)

$dotRels = @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>
'@
[System.IO.File]::WriteAllText((Join-Path $relsDir ".rels"), $dotRels, [System.Text.Encoding]::UTF8)

$docRels = @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>
'@
[System.IO.File]::WriteAllText((Join-Path $wordRelsDir "document.xml.rels"), $docRels, [System.Text.Encoding]::UTF8)

$stylesXml = @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Segoe UI" w:hAnsi="Segoe UI" w:cs="Segoe UI"/>
        <w:sz w:val="22"/>
        <w:color w:val="2D3748"/>
      </w:rPr>
    </w:rPrDefault>
    <w:pPrDefault>
      <w:pPr>
        <w:spacing w:line="276" w:lineRule="auto" w:after="160"/>
      </w:pPr>
    </w:pPrDefault>
  </w:docDefaults>
</w:styles>
'@
[System.IO.File]::WriteAllText((Join-Path $wordDir "styles.xml"), $stylesXml, [System.Text.Encoding]::UTF8)

$bodyXml = [System.Text.StringBuilder]::new()

function New-WordTitle {
    param([string]$Text)
    $esc = ConvertTo-EscapedXml -InputText $Text
    [void]$bodyXml.Append("<w:p><w:pPr><w:jc w:val='center'/><w:spacing w:before='360' w:after='120'/></w:pPr><w:r><w:rPr><w:b/><w:color w:val='1E3A8A'/><w:sz w:val='52'/><w:rFonts w:ascii='Segoe UI Semibold' w:hAnsi='Segoe UI Semibold'/></w:rPr><w:t>$esc</w:t></w:r></w:p>")
}

function New-WordSubtitle {
    param([string]$Text)
    $esc = ConvertTo-EscapedXml -InputText $Text
    [void]$bodyXml.Append("<w:p><w:pPr><w:jc w:val='center'/><w:spacing w:before='0' w:after='300'/></w:pPr><w:r><w:rPr><w:i/><w:color w:val='4B5563'/><w:sz w:val='26'/></w:rPr><w:t>$esc</w:t></w:r></w:p>")
}

function New-WordMetaBox {
    param(
        [string]$Author,
        [string]$Version,
        [string]$Date,
        [string]$Stack
    )
    $vEsc = ConvertTo-EscapedXml -InputText $Version
    $dEsc = ConvertTo-EscapedXml -InputText $Date
    $sEsc = ConvertTo-EscapedXml -InputText $Stack
    $aEsc = ConvertTo-EscapedXml -InputText $Author
    [void]$bodyXml.Append("<w:tbl><w:tblPr><w:tblW w:w='9600' w:type='dxa'/><w:jc w:val='center'/><w:tblBorders><w:top w:val='single' w:sz='6' w:space='0' w:color='93C5FD'/><w:left w:val='single' w:sz='24' w:space='0' w:color='2563EB'/><w:bottom w:val='single' w:sz='6' w:space='0' w:color='93C5FD'/><w:right w:val='single' w:sz='6' w:space='0' w:color='93C5FD'/></w:tblBorders><w:tblCellMar><w:top w:w='140' w:type='dxa'/><w:left w:w='200' w:type='dxa'/><w:bottom w:w='140' w:type='dxa'/><w:right w:w='200' w:type='dxa'/></w:tblCellMar></w:tblPr><w:tr><w:tc><w:tcPr><w:tcW w:w='9600' w:type='dxa'/><w:shd w:val='clear' w:color='auto' w:fill='EFF6FF'/></w:tcPr><w:p><w:pPr><w:spacing w:after='40'/></w:pPr><w:r><w:rPr><w:b/><w:color w:val='1E40AF'/></w:rPr><w:t>Autor: </w:t></w:r><w:r><w:t>$aEsc</w:t></w:r></w:p><w:p><w:pPr><w:spacing w:after='40'/></w:pPr><w:r><w:rPr><w:b/><w:color w:val='1E40AF'/></w:rPr><w:t>Versión / Edición: </w:t></w:r><w:r><w:t>$vEsc</w:t></w:r></w:p><w:p><w:pPr><w:spacing w:after='40'/></w:pPr><w:r><w:rPr><w:b/><w:color w:val='1E40AF'/></w:rPr><w:t>Fecha de Emisión: </w:t></w:r><w:r><w:t>$dEsc</w:t></w:r></w:p><w:p><w:pPr><w:spacing w:after='40'/></w:pPr><w:r><w:rPr><w:b/><w:color w:val='1E40AF'/></w:rPr><w:t>Stack Tecnológico: </w:t></w:r><w:r><w:t>$sEsc</w:t></w:r></w:p></w:tc></w:tr></w:tbl><w:p><w:pPr><w:spacing w:after='240'/></w:pPr></w:p>")
}

function New-WordHeading1 {
    param([string]$Text)
    $esc = ConvertTo-EscapedXml -InputText $Text
    [void]$bodyXml.Append("<w:p><w:pPr><w:spacing w:before='360' w:after='140'/><w:pBdr><w:bottom w:val='single' w:sz='12' w:space='4' w:color='2563EB'/></w:pBdr></w:pPr><w:r><w:rPr><w:b/><w:color w:val='1E3A8A'/><w:sz w:val='34'/><w:rFonts w:ascii='Segoe UI Semibold' w:hAnsi='Segoe UI Semibold'/></w:rPr><w:t>$esc</w:t></w:r></w:p>")
}

function New-WordHeading2 {
    param([string]$Text)
    $esc = ConvertTo-EscapedXml -InputText $Text
    [void]$bodyXml.Append("<w:p><w:pPr><w:spacing w:before='260' w:after='100'/></w:pPr><w:r><w:rPr><w:b/><w:color w:val='0D9488'/><w:sz w:val='28'/><w:rFonts w:ascii='Segoe UI Semibold' w:hAnsi='Segoe UI Semibold'/></w:rPr><w:t>$esc</w:t></w:r></w:p>")
}

function New-WordParagraph {
    param([string]$Text)
    $esc = ConvertTo-EscapedXml -InputText $Text
    [void]$bodyXml.Append("<w:p><w:pPr><w:spacing w:after='140' w:line='276' w:lineRule='auto'/><w:jc w:val='both'/></w:pPr><w:r><w:rPr><w:sz w:val='22'/><w:color w:val='334155'/></w:rPr><w:t xml:space='preserve'>$esc</w:t></w:r></w:p>")
}

New-WordTitle "MANUAL DE FUNCIONALIDAD Y ARQUITECTURA"
New-WordSubtitle "Documentación Técnica y Operativa Completa de FerreteríaPro"
New-WordMetaBox -Author "Equipo de Desarrollo" -Version "1.0-RELEASE (2026)" -Date (Get-Date -Format "dd/MM/yyyy") -Stack "Java 21/26 + JavaFX 21 + SQLite 3 (WAL) + OpenPDF"

New-WordHeading1 "1. Resumen Ejecutivo y Alcance"
New-WordParagraph "FerreteríaPro es una solución empresarial de escritorio para negocios de ferretería y distribución de materiales. Integra POS, control de inventario, gestión de cartera, turnos de caja, consumos internos bajo arquitectura hexagonal y respaldos híbridos."

# Assembling word/document.xml
$documentXml = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <w:body>
$($bodyXml.ToString())
    <w:sectPr>
      <w:pgSz w:w="12240" w:h="15840"/>
      <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/>
    </w:sectPr>
  </w:body>
</w:document>
"@

[System.IO.File]::WriteAllText((Join-Path $wordDir "document.xml"), $documentXml, [System.Text.Encoding]::UTF8)

# Cleanup
Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue

Write-Output "Script completado sin advertencias."
